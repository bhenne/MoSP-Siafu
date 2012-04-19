#!/usr/bin/env python
# run using `python -O client.py` to suppress DEBUG output

import socket

__author__ = "B. Henne"
__contact__ = "henne@dcsec.uni-hannover.de"
__copyright__ = "(c) 2012, DCSec, Leibniz Universitaet Hannover, Germany"
__license__ = "GPLv3"

STEP_DONE_PUSH = '\xF9'
STEP_DONE = '\xFA'
SIM_ENDED = '\xFB'
ACK = '\xFC'
FIELD_SEP = '\xFD'
MSG_SEP = '\xFE'
MSG_END = '\xFF'
QUIT = '\x00'
GET_MODE = '\x01'
PUT_MODE = '\x02'
STEP = '\x03'
IDENT = '\x04'

s = None
steps = 0

def connect(host='localhost', port=4444):
  global s
  if __debug__: print 'Connect to %s:%s' % (host, port)
  s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  s.connect((host, port))

def exit_sim():
  s.send(QUIT)

def close(exit=True):
  if (exit == True):
    if __debug__: print "Shutdown sim"
    exit_sim()
  if __debug__: print "Close connection"
  s.close()

def recv_until(endsymbol, include_endsymbol=True):
  d = ''
  r = ''
  while (d != endsymbol):
    d = s.recv(1)
    if (d != endsymbol) or (include_endsymbol == True):
      r += d
  return r

def identify():
  if __debug__: print "--> IDENTIFY"
  s.send(IDENT)
  i = recv_until(MSG_END, False)
  if __debug__: print '<-- %s' % i.split(FIELD_SEP)

def step(n=1):
  global steps
  if __debug__: print "--> STEP %s" % n
  s.send(STEP)
  s.send(str(n))
  s.send(MSG_END)
  steps += 1
  d = s.recv(1)
  if (d == ACK):
    if __debug__: print "<-- ACK"
  else:
    if __debug__: print "!!! ACK wanted, got %s" % d
  d = s.recv(1)
  if (d == STEP_DONE):
    if __debug__: print "<-- STEP(S) DONE"
  elif (d == STEP_DONE_PUSH):
    if __debug__: print "<-- STEP(S) DONE, PUSHING DATA"
    blob = recv_until(MSG_END, False)
    if __debug__: print "    received %s bytes" % len(blob)
    if __debug__: print "--> ACK"
    s.send(ACK)
    if len(blob) > 0:
      print "%5i" % steps
      msgs = blob.split(MSG_SEP)
      for msg in msgs:
        print "  > %s" % msg.split(FIELD_SEP)
  else:
    if __debug__: print "!!! STEP(S)_DONE wanted, got %s" % d

def get():
  if __debug__: print "--> GET"
  s.send(GET_MODE)
  if __debug__: print "<-- DATA"
  blob = recv_until(MSG_END, False)
  if __debug__: print "--> ACK"
  s.send(ACK)
  if len(blob) > 0:
    print "%5i" % steps
    msgs = blob.split(MSG_SEP)
    for msg in msgs:
      print "  > %s" % msg.split(FIELD_SEP) 

def put(data=''):
  if data == '':
    return
  if __debug__: print "--> PUT"
  s.send(PUT_MODE)
  if __debug__: print "--> DATA"
  s.sendall(data)
  d = s.recv(1)
  if (d == ACK):
    if __debug__: print "<-- ACK"
  else:
    if __debug__: print "!!! ACK wanted, got %s" % d
  if len(data) > 0:
    print "%5i" % steps
    try:
      out = ''
      msgs = data[:-1].split(MSG_SEP)
      for msg in msgs:
       out += "  < %s\n" % msg.split(FIELD_SEP)
      print out[:-1]
    except:
      print "  %s" % data 

def step_get_put(n=1, put_data=''):
  for i in xrange(0,n):
    step(1)
    get()
    if (put_data != ''):
      put(put_data)

def step_put(n=1, put_data=''):
  """Steps n times, may receive pushed data, puts if needed."""
  for i in xrange(0,n):
    step(1)
    if (put_data != ''):
      put(put_data)


TEST_PUT_DATA_Z = 'MobileInfectWiggler'+FIELD_SEP+'{ "p_id":42, "p_infected_mobile":true, "p_CAFE_WAITING_MIN":300, "p_CAFE_WAITING_MAX":900, "p_TIME_INTERNET_CAFE":60, "p_need_internet_offset":4, "p_INFECTION_DURATION":15, "p_INFECTION_RADIUS":50, "p_infection_time":13 }'+MSG_END
TEST_PUT_DATA_H = 'MobileInfectWiggler'+FIELD_SEP+'{ "p_id":23, "p_infected_mobile":false, "p_CAFE_WAITING_MIN":300, "p_CAFE_WAITING_MAX":900, "p_TIME_INTERNET_CAFE":60, "p_need_internet_offset":1, "p_INFECTION_DURATION":15, "p_INFECTION_RADIUS":50, "p_infection_time":-1 }'+MSG_END

TEST_PUT_DATA_LOG = 'LogTomain'+FIELD_SEP+'LP0wnedPersonDataChannelForLogMessages!'+MSG_END

def test():
  connect()
  identify()
  step_put(60, '')
  step_put(1, TEST_PUT_DATA_H)
  step_put(30, '')
  step_put(1, TEST_PUT_DATA_Z)
  step_put(1200, '')
  close()

if __name__ == '__main__':
  test()

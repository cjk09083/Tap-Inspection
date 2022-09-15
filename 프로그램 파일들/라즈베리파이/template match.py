import cv2
import numpy as np

import RPi.GPIO as gp
import time


#motor setting
CLK_L = 17
DIR_L = 18
CLK_R = 22
DIR_R = 23
MODE1 = 9
MODE2 = 25
ENABLE = 8
TX = 26
RX = 16

#ROI
ROI_x = 330 
ROI_y = 205

#ROI threshold
ROI_threshold = 3

#angle setting
angle = int(90)

#speed setting
speed = 0.0001

#ignoring warning
gp.setwarnings (False)
#select BCM or GPIO pin number
gp.setmode(gp.BCM)

#pinMode
gp.setup(CLK_L, gp.OUT)
gp.setup(DIR_L, gp.OUT)
gp.setup(CLK_R, gp.OUT)
gp.setup(DIR_R, gp.OUT)
gp.setup(MODE1, gp.OUT)
gp.setup(MODE2, gp.OUT)
gp.setup(ENABLE, gp.OUT)
gp.setup(TX, gp.OUT)
gp.setup(RX, gp.IN)

#motor mode
gp.output(MODE1, False)
gp.output(MODE2, True)
gp.output(ENABLE, True)

#TX pin clear
gp.output(TX, False)

count=[0,0,0,0]
threshold=[0.85,0.83,0.85]

templates = [cv2.imread('nut'+str(i)+'.png',cv2.IMREAD_GRAYSCALE) for i in range(0,3)]
w1,h1=templates[0].shape[::-1]
w2,h2=templates[1].shape[::-1]
w3,h3=templates[2].shape[::-1]

cap = cv2.VideoCapture(-1)



while True:
    
    _, frame = cap.read()
    gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    cv2.circle(frame,(ROI_x, ROI_y),10,(0,0,255),2)
    
    result1=cv2.matchTemplate(gray_frame,templates[0],cv2.TM_CCOEFF_NORMED)
    result2=cv2.matchTemplate(gray_frame,templates[1],cv2.TM_CCOEFF_NORMED)
    result3=cv2.matchTemplate(gray_frame,templates[2],cv2.TM_CCOEFF_NORMED)
    
    loc1=np.where(result1>=threshold[0])
    loc2=np.where(result2>=threshold[1])
    loc3=np.where(result3>=threshold[2])
  
    
    for pt1 in zip(*loc1[::-1]):
        cv2.rectangle(frame, pt1, (pt1[0]+w1,pt1[1]+h1),(0,0,255),1)
        cv2.circle(frame, (pt1[0] + int(w1/2), pt1[1] + int(h1/2)), 1, (0,0,255),-1)
        cv2.putText(frame, "M3", (pt1[0], pt1[1]), cv2.FONT_HERSHEY_DUPLEX, 1, (0, 0, 255))
        count[0]=count[0]+1
        x_center = pt1[0] + int(w1/2)
        y_center = pt1[1] + int(h1/2)
    
    for pt2 in zip(*loc2[::-1]):
        cv2.rectangle(frame, pt2, (pt2[0]+w2,pt2[1]+h2),(0,0,255),1)
        cv2.circle(frame, (pt2[0] + int(w2/2), pt2[1] + int(h2/2)), 1, (0,0,255),-1)
        cv2.putText(frame, "M4", (pt2[0], pt2[1]), cv2.FONT_HERSHEY_DUPLEX, 1, (0, 0, 255),)
        count[1]=count[1]+1
        x_center = pt2[0] + int(w2/2)
        y_center = pt2[1] + int(h2/2)
    
    for pt3 in zip(*loc3[::-1]):
        cv2.rectangle(frame, pt3, (pt3[0]+w3,pt3[1]+h3),(0,0,255),1)
        cv2.circle(frame, (pt3[0] + int(w3/2), pt3[1] + int(h3/2)), 1, (0,0,255),-1)
        cv2.putText(frame, "M5", (pt3[0], pt3[1]), cv2.FONT_HERSHEY_DUPLEX, 1, (0, 0, 255))
        count[2]=count[2]+1
        x_center = pt3[0] + int(w3/2)
        y_center = pt3[1] + int(h3/2)
    
    
    
    print (x_center, y_center)
    
    
    
    if x_center > ROI_x-5 and x_center< ROI_x+5 and y_center > ROI_y-5 and y_center < ROI_y+5:
       cv2.circle(frame,(ROI_x, ROI_y),10,(0,255,0),2)
       #send GOOD signal to arduino(pinTX)
       gp.output(TX, True)
       
    else:
        #send BAD signal to arduino(pinTX)
        gp.output(TX, False)

       
       
    if gp.input(RX)==1:
        if x_center < ROI_x-ROI_threshold:
            gp.output(DIR_R, False)
            for i in range(angle):
                gp.output(CLK_R, True)
                time.sleep(speed)
                gp.output(CLK_R, False)
                time.sleep(speed)
            
        elif x_center > ROI_x+ROI_threshold:
            gp.output(DIR_R, True)
            for i in range(angle):
                gp.output(CLK_R, True)
                time.sleep(speed)
                gp.output(CLK_R, False)
                time.sleep(speed)
            
            
            
        if y_center < ROI_y-ROI_threshold:
            gp.output(DIR_L, True)
            for i in range(angle):
                gp.output(CLK_L, True)
                time.sleep(speed)
                gp.output(CLK_L, False)
                time.sleep(speed)
            
        elif y_center > ROI_y+ROI_threshold:
            gp.output(DIR_L, False)
            for i in range(angle):
                gp.output(CLK_L, True)
                time.sleep(speed)
                gp.output(CLK_L, False)
                time.sleep(speed)
        
    
    cv2.imshow("Frame", frame)
    
    key = cv2.waitKey(1)
    if key == 27: #PRESS "ESC" KEY
        break
    
    elif key == 114: #PRESS "R" KEY
        gp.output(TX, False)
        gp.output(DIR_L, True)
        for i in range(25000):
            gp.output(CLK_L, True)
            time.sleep(speed)
            gp.output(CLK_L, False)
            time.sleep(speed)
            
        cv2.imshow("Frame", frame)
        key = cv2.waitKey(0)
            
        if key == 115: #PRESS "S" KEY
            gp.output(DIR_L, False)
            for i in range(25000):
                gp.output(CLK_L, True)
                time.sleep(speed)
                gp.output(CLK_L, False)
                time.sleep(speed)

cap.release()
cv2.destroyAllWindows()


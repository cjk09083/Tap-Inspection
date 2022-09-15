# Tap-Inspection

<div>
<img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=Android&logoColor=white"/>
<img src="https://img.shields.io/badge/Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white"/></a>
<img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=Python&logoColor=white"/></a>

<a href="https://ieeexplore.ieee.org/document/9268255" target="_blank">
<img src="https://img.shields.io/badge/관련논문-FF0000?style=for-the-badge&logo=Apache&logoColor=white"/>
</a>
</div>

## Tap-Inspection 나사선 검사기


## 목적
- 농장 옆에 설치된 반사판을 회전시켜 최적의 광량을 농장에 공급해주는 자동제어 스마트팜 개발
- 스마트팜의 상태를 어플리케이션으로 모니터링하고 설치된 각종 기기를 원격 제어

## 담당 
- 안드로이드 어플리케이션
- 웹서버 php, sql 
- 아두이노 NANO 와 센서, 모터 제어 (I2C, Serial, PWM)
- 아두이노 WEMOS 와 서버 통신 (Wifi) 
- 앱 <-> 서버 <-> 아두이노 통신 (HTTP, MQTT)

## 기능

### 1. 농장 환경에 따른 최적화 자동제어
 - 농장의 온습도, 이산화탄소량을 측정해 기준치를 벗어나면 냉방기, 환풍기등 외부기기를 제어
 - 일정 시간마다 반사판을 회전시켜 최적/최대 광량을 공급하는 각도로 제어 및 유지

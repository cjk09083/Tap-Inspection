# Tap-Inspection

<div>
<img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=Android&logoColor=white"/>
<img src="https://img.shields.io/badge/Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white"/></a>
<img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=Python&logoColor=white"/></a>

<a href="https://ieeexplore.ieee.org/document/9268255" target="_blank">
<img src="https://img.shields.io/badge/관련논문-FF0000?style=for-the-badge&logo=Apache&logoColor=white"/>
</a>
</div>

## 나사선 검사기

## 목적
- 나사선의 불량 여부를 자동으로 판별하고 이를 어플리케이션과 연동하는 검사기 개발

## 담당 
- 안드로이드 어플리케이션
- 아두이노와 PLC, 모터 제어 (SPI, Serial, PWM)
- 앱 <-> 아두이노 통신 (Bluetooth)

## 기능

### 1. 나사선의 불량 여부 판별
 - 모터끝에 나사를 달아 나사선에 삽입/회전시키고 일정량 이상의 역토크가 발생시 에러감지 
<div align="center">
<img src="https://github.com/cjk09083/Tap-Inspection/blob/main/%EC%82%AC%EC%A7%84%20%EB%B0%8F%20%EC%98%81%EC%83%81/%EB%8F%99%EC%9E%91%EC%98%81%EC%83%81.gif" width="30%"/>
</div></br>

### 2. 어플리케이션 연동
 - 검사 변수를 설정 및 동작 제어하고 검사 결과를 모니터링
<div align="center">
<img src="https://github.com/cjk09083/Tap-Inspection/blob/main/%EC%82%AC%EC%A7%84%20%EB%B0%8F%20%EC%98%81%EC%83%81/%EC%96%B4%ED%94%8C%EB%A6%AC%EC%BC%80%EC%9D%B4%EC%85%98%20%ED%99%94%EB%A9%B4.jpg" width="30%"/>
</div></br>

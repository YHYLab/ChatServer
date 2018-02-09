# ChatServer
채팅 서버

## DB,포트 설정파일 
  - config.properties
  
## 컴파일 
   - Eclipse 에서 소스를 clone 한 후 
   - 컴파일 옵션에서 Maven Build의 Goals 를 "clean compile install" 설정
   - Skip Test옵션을 체크
   - Run 실행
  
## 배포
   - 서버에 자바 1.7 버전을 설치
   - 빌드하여 생성된 target/wonderflickChat.jar 파일을 scp로 서버에 전송
   - 소스중의 chatServerStart.sh를 복사하여 .jar파일과 같은 위치에 놓은 후 실행.
   - 실 포트번호가 8092 이므로 사전 서버의 포트를 개방
 
## 채팅로그 
   - chat_log 테이블에 저장 - 비동기로 logback으로 작동
   
## 실시간 동접자
   - concurrent_user_number 테이블에 저장
   - applicationContext.xml 에서 jobScheduler 의 cron 에서 저장 기간 조정
# ChatServer

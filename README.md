## Open API와 통신하여 Spring 서버 개발하기
- 개인 학습용 repo Open API를 활용하여 데이터를 통신하여 가공 후 반환하기
## 기술 스택
- Gradle - Groovy
- Java 17
- Springboot 3.1.8
- MySQL
- Junit4

## 활용 공공데이터

- 기상청 전국 해수욕장 날씨 조회서비스
- 해수욕장 단기예보 조회

## Overview
```
현업에서 Open API를 활용해서 데이터를 수집하고 이를 가공하는 작업을 맡게 될 수 있다.
서버 어플리케이션에서 Open API를 써본 경험이 많지 않아서 미리 연습을 해본다.
```
## 요구사항
- Open API와 통신하여 데이터 받아오기
- 요청 날짜에 따른 값 반환하기
- JSON 데이터를 Java Object에 Mapping하여 반환하기

## 공공데이터란
```
공공데이터란 공공기관이 만들어내는 모든 자료나 정보, 국민 모두의 소통과 협력을 이끌어내는 공적인 정보를 말한다.

각 공공기관이 공유한 공공데이터 목록과 국민에게 개방할 수 있는 공공데이터를 포털에 등록하면
모두가 공유할 수 있는 양질의 공공데이터로 재탄생하게 된다.

-공공데이터 포털 개요 참고-
```
## 공공데이터 활용 신청하기
```
우선 자신이 활용할 데이터를 서치한다.
기상청전국 해수욕장 날씨 조회서비스 OpenAPI를 활용하기 위해 '활용 신청'을 했다.
```
![image](https://github.com/mr-won/OpenAPI/assets/58906858/964fbab7-5070-4e16-b19d-f79909dc1074)
```
신청한 API를 누르면 개발계정 상세보기 페이지가 나오는데 여기서 출력되는 일반 인증키를 사용하면 된다.
```
## Open API 활용가이드
```
요청 URL과 요청에 필요한 파라미터를 확인해준다.
```
![image](https://github.com/mr-won/OpenAPI/assets/58906858/37a3397b-ba86-4392-b192-8ed06c7ed0a2)
```
응답 메시지 명세도 확인해준다.
요청을 하면 이런 응답 파라미터가 온다
```
![image](https://github.com/mr-won/OpenAPI/assets/58906858/8be36e64-49f8-42fc-b1d6-3ee924c68251)
![image](https://github.com/mr-won/OpenAPI/assets/58906858/6a3ee258-e603-4e8b-a7fc-eb3bf6f7734b)
## Spring과 연동하기
[Spring project 생성](https://start.spring.io/)
- Gradle - Groovy
- Java 17
- 3.1.0
- MySQL
- Junit4
## Controller에서 통신하기
```JAVA
@RestController
@RequestMapping("/api")
public class ForecastController {
    @Value("${openApi.serviceKey}")
    private String serviceKey;

    @Value("${openApi.callBackUrl}")
    private String callBackUrl;

    @Value("${openApi.dataType}")
    private String dataType;

    @GetMapping("/forecast")
    public ResponseEntity<String> callForecastApi(
            @RequestParam(value="base_time") String baseTime,
            @RequestParam(value="base_date") String baseDate,
            @RequestParam(value="beach_num") String beachNum
    ){
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        String result = null;

        String urlStr = callBackUrl +
                "serviceKey=" + serviceKey +
                "&dataType=" + dataType +
                "&base_date=" + baseDate +
                "&base_time=" + baseTime +
                "&beach_num=" + beachNum;

        try {
            URL url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();
            stream = getNetworkConnection(urlConnection);
            result = readStreamToString(stream);

            if (stream != null) stream.close();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환 */
    private InputStream getNetworkConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setConnectTimeout(3000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);

        if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code : " + urlConnection.getResponseCode());
        }

        return urlConnection.getInputStream();
    }

    /* InputStream을 전달받아 문자열로 변환 후 반환 */
    private String readStreamToString(InputStream stream) throws IOException{
        StringBuilder result = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

        String readLine;
        while((readLine = br.readLine()) != null) {
            result.append(readLine + "\n\r");
        }

        br.close();

        return result.toString();
    }
}
```
## JSON Viewer
![image](https://github.com/mr-won/OpenAPI/assets/58906858/ab91f696-fa74-4e1d-b55f-5b1f9d554903)
```
크롬 확장프로그램 중 JSON Viewer를 사용하여 JSON Parsing한 데이터를 깔끔하게 볼 수 있다.
```






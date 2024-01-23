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
## JSON deserialize
이제 데이터를 사용하려면 적절하게 가공을 해주어야 한다.    
JSON deserialize 작업을 해주어 데이터를 가공해 Java Object에 mapping하여 값을 반환한다.    
   
JSON 라이브러리는 정말 많은데 나는 그중 Jackson 라이브러리를 사용해볼 것이다.    

스프링부트는 spring-boot-starter-web에 Jackson 라이브러리를 제공하고 있어서 Json의 직렬/역직렬화에는 Jackson을 사용한다.    
     
deserialize(Json > VO)는 아래와 같은 순서를 따른다.   
```
기본 생성자로 객체를 생성한다.
public 필드 또는 public의 getter/setter로 필드를 찾아 binding한다.
```

## response 구조 파악하기
response JSON 응답을 적절하게 binding 해주어야 하는데 이 과정이 몹시 몹 시 귀찮다!    
우선 response JSON의 구조를 파악해서 depth를 구분한다.     
```JAVA
{
  "response": {
    "header": {
      "resultCode": "00",
      "resultMsg": "NORMAL_SERVICE"
    },
    "body": {
      "dataType": "JSON",
      "items": {
        "item": [
          {
            "beachNum": "1",
            "baseDate": "20230525",
            "baseTime": "1100",
            "category": "TMP",
            "fcstDate": "20230525",
            "fcstTime": "1200",
            "fcstValue": "20",
            "nx": 49,
            "ny": 124
          },
          (...)
          {
            "beachNum": "1",
            "baseDate": "20230525",
            "baseTime": "1100",
            "category": "PCP",
            "fcstDate": "20230525",
            "fcstTime": "1200",
            "fcstValue": "강수없음",
            "nx": 49,
            "ny": 124
          }
        ]
      },
      "pageNo": 1,
      "numOfRows": 10,
      "totalCount": 737
    }
  }
}
```
```
depth 1 : response
depth 2 : header, body
depth 3(header) : resultCode, resultMsg
depth 3(body) : dataType, items, pageNo, numOfRows, totalCount
depth 4(items) : item(array)
depth 5(item) : beachNum, baseDate, baseTime, category, fcstDate, fcstTime, fcstValue, nx, ny
```
우선 응답 데이터를 mapping 시킬 VO를 생성한다.
## FcstItems
```JAVA
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FcstItems {
    @JsonProperty("item")
    private List<FcstItem> fcstItems;
}
```
## FcstItem
```JAVA
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FcstItem {
    // 해변코드
    @JsonProperty("beachNum")
    private int beachNum;

    // 발표일자
    @JsonProperty("baseDate")
    private String baseDate;

    // 발표시각
    @JsonProperty("baseTime")
    private String baseTime;

    // 자료구분코드
    @JsonProperty("category")
    private String category;

    // 예보일자
    @JsonProperty("fcstDate")
    private String fcstDate;

    // 예보시간
    @JsonProperty("fcstTime")
    private String fcstTime;

    // 예보 값
    @JsonProperty("fcstValue")
    private String fcstValue;

    // X좌표
    @JsonProperty("nx")
    private int nx;

    // Y좌표
    @JsonProperty("ny")
    private int ny;
}
```
## binding 하기
binding하는 방법은 두 가지가 있다고
```
1. Custom deserializer 작성하기    
2. 어노테이션 사용하기    
```
## 1. Custom deserializer
Deserializer를 별도의 클래스에 코드로 작성하면 DTO 내부 코드가 깔끔하고 재사용면에서 장점을 가진다.            
그러나 dto마다 deserializer를 작성해주어야 하는 경우와 같이 클래스의 수가 많아지는 일이 발생할 수 있다.    

## FcstItemDeserializer
```JAVA
public class FcstItemDeserializer extends JsonDeserializer<FcstItems> {

    private final ObjectMapper objectMapper;

    public FcstItemDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FcstItems deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode itemNode = node.findValue("item");

        List<FcstItem> items = Arrays.stream(objectMapper.treeToValue(itemNode, FcstItem[].class)).toList();

        return new FcstItems(items);
    }
}
```
com.fasterxml.jackson.databind.JsonDeserializer를 구현하는 Deserializer 객체를 생성한다.     
JsonNode를 통해 mapping 해주었다.
```
get() : 노드의 필드를 찾고 없으면 null을 반환한다.
e.g. node.get("body").get("totalCount").asInt();
path() : 노드의 필드를 찾고 없으면 MissingNode를 반환한다.
findValue() : 노드와 자식 노드들에서 필드를 찾고 없으면 null을 반환한다.
```
- 순차적인 접근은 get(), path()
- 노드 하위 전체에서 필드를 찾고 싶으면 findValue()
- 이때 동일 필드명이 있는 경우 잘못된 필드를 찾을 수도 있다.
- item은 동일 필드명이 없기 때문에 findValue() 방식으로 접근했다.

List 값을 받으려면 objectMapper.treetoValue()를 활용하여 배열로 받아 toList() 해주어야 한다.    
## FcstItems
```JAVA
@Data
@JsonDeserialize(using = FcstItemDeserializer.class)
public class FcstItems {
    @JsonProperty("item")
    private List<FcstItem> fcstItems;

    public FcstItems(List<FcstItem> fcstItems) {
        this.fcstItems = fcstItems;
    }
}
```
FcstItems에 Deserialize할 때, 어떤 Deserializer를 사용할지 명시해주어야 한다.    
@JsonDeserializer 어노테이션을 추가하여 class를 설정해주었다.    

## 2. annotation
Deserializer를 재사용할 일이 많이 없거나, DTO마다 별도의 Deserializer가 필요한 경우     
annotation을 사용해주는 방법도 있다.    
## FcstItems
```JAVA
@Data
@AllArgsConstructor
public class FcstItems {
    @JsonProperty("item")
    private List<FcstItem> fcstItems;

    @JsonCreator
    public FcstItems(@JsonProperty("response")JsonNode node) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode itemNode = node.findValue("item");
        this.fcstItems = Arrays.stream(objectMapper.treeToValue(itemNode, FcstItem[].class)).toList();
    }
}
```
@JsonCreator 과 @JsonProperty 어노테이션을 VO 안에서 사용해주었다.    

@JsonCreator은 기본생성자, setter 조합을 대체 하기때문에 @NoArgsConstructor가 필요없다.    
객체를 생성하고 필드를 생성과 동시에 채워 setter없이 immutable한 객체를 얻을 수 있다는 장점이 있다.    

@JsonProperty로 depth 1의 response를 가져와 주었다.     
## Service 코드 짜기
```JAVA
@Service
public class ForecastService {

    public FcstItems parsingJsonObject(String json) {
        FcstItems items = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            items = mapper.readValue(json, FcstItems.class);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
```
## 결과값 확인하기
```JAVA
{
    "item": [
        {
            "beachNum": 1,
            "baseDate": "20230524",
            "baseTime": "1100",
            "category": "TMP",
            "fcstDate": "20230524",
            "fcstTime": "1200",
            "fcstValue": "20",
            "nx": 49,
            "ny": 124
        },
        (...)
        {
            "beachNum": 1,
            "baseDate": "20230524",
            "baseTime": "1100",
            "category": "PCP",
            "fcstDate": "20230524",
            "fcstTime": "1200",
            "fcstValue": "강수없음",
            "nx": 49,
            "ny": 124
        }
    ]
}
```
## 추가 가공하기
이제 json > object로의 mapping 작업은 모두 종료됐다. 추가적으로 Open API로 가져온 코드 값들을 해석해줄 일이 남았다.    
category의 경우 별첨 자료에 코드 해석에 대한 내용이 나와있다.     
클라이언트측에 값을 줄 때 해석한 값을 주는 것이 좋을 것 같아서 enum 클래스를 생성했다.    
## CategoryCode
```JAVA
public enum CategoryCode {
    POP("강수확률", "%"),
    PTY("강수형태", ""),
    PCP("1시간 강수량", "mm"),
    REH("습도", "%"),
    SNO("1시간 신적설", "cm"),
    SKY("하늘상태", ""),
    TMP("1시간 기온", "℃"),
    TMN("아침 최저기온", "℃"),
    TMX("낮 최고기운", "℃"),
    UUU("풍속(동서성분)", "m/s"),
    VVV("풍속(남북성분)", "m/s"),
    WAV("파고", "M"),
    VEC("풍향", "deg"),
    WSD("풍속", "m/s");
    >
    private final String name;
    private final String unit;
    CategoryCode(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }
    public String getName() { return name; }
    public String getUnit() { return unit; }
>
    public static String getCodeInfo(String name, String value) {
        CategoryCode c = CategoryCode.valueOf(name);
        if(c == CategoryCode.PTY) {
            switch (value) {
                case "0":
                    return "없음";
                case "1":
                    return "비";
                case "2":
                    return "비/눈";
                case "3":
                    return "눈";
                case "4":
                    return "소나기";
            }
        } else if(c == CategoryCode.SKY) {
            switch(value) {
                case "1":
                    return "맑음";
                case "3":
                    return "구름많음";
                case "4":
                    return "흐림";
            }
        }
        return value;
    }
}
```
## FcstItem
category 코드를 해석하면 담아줄 필드를 하나 추가했다.        
json과 mapping 될 때 json에는 없는 값이므로 @jsonIgnoreProperties(ignoreUnknown = true)를 추가해주었다.        
```JAVA
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FcstItem {
    // 해변코드
    @JsonProperty("beachNum")
    private int beachNum;

    (...)
    // 예보 값
    @JsonProperty("fcstValue")
    private String fcstValue;
    
    private String categoryName;
}
```
## ForecastService
CategoryCode를 활용하는 코드를 추가한다.
```JAVA
@Service
public class ForecastService {

    public FcstItems parsingJsonObject(String json) {
        FcstItems result = new FcstItems(new ArrayList<>());
        try {
            ObjectMapper mapper = new ObjectMapper();
            FcstItems items = mapper.readValue(json, FcstItems.class);

            for(FcstItem item : items.getFcstItems()) {
                result.getFcstItems().add(decodeCategory(item));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private FcstItem decodeCategory(FcstItem item) {
        String name = CategoryCode.valueOf(item.getCategory()).getName();
        String value = CategoryCode.getCodeValue(item.getCategory(), item.getFcstValue());
        String unit = CategoryCode.valueOf(item.getCategory()).getUnit();

        item.setCategoryName(name);
        item.setFcstValue(value + unit);
        return item;
    }
}
```
## 최종 결과 JSON Viewer
![image](https://github.com/mr-won/OpenAPI/assets/58906858/ab91f696-fa74-4e1d-b55f-5b1f9d554903)
```
크롬 확장프로그램 중 JSON Viewer를 사용하여 JSON Parsing한 데이터를 깔끔하게 볼 수 있다.
```






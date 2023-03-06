![](https://velog.velcdn.com/images/raycho521/post/c59a7183-9982-4284-a348-3a4177f8eb68/image.jpeg
)

[블로그 설명](https://velog.io/@raycho521/AWS-S3-1-S3-%EB%B2%84%ED%82%B7-%EC%83%9D%EC%84%B1%ED%95%98%EA%B8%B0-%EB%B3%B4%EC%95%88%EC%84%A4%EC%A0%95)

### S3ResourceStorage

```java

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ResourceStorage {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3Client amazonS3Client;

    public void store(String fullPath, MultipartFile multipartFile) {
        File file = new File(MultipartUtil.getLocalHomeDirectory(), fullPath);
        try {
            multipartFile.transferTo(file);
            amazonS3Client.putObject(new PutObjectRequest(bucket, fullPath, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException();
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public ResponseEntity<byte[]> getObject(String storedFileName) throws IOException {
        S3Object o = amazonS3Client.getObject(new GetObjectRequest(bucket, storedFileName));
        S3ObjectInputStream objectInputStream = o.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectInputStream);

        String fileName = URLEncoder.encode(storedFileName, "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }
}
```

### Util
```java
@Slf4j
public class MultipartUtil {
    private static final String BASE_DIR = "video";

    /**
     * 로컬에서의 사용자 홈 디렉토리 경로를 반환합니다.
     */
    public static String getLocalHomeDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * 새로운 파일 고유 ID를 생성합니다.
     * @return 36자리의 UUID
     */
    public static String createFileId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Multipart 의 ContentType 값에서 / 이후 확장자만 잘라냅니다.
     * @param contentType ex) image/png
     * @return ex) png
     */
    public static String getFormat(String contentType) {
        if (StringUtils.hasText(contentType)) {
            return contentType.substring(contentType.lastIndexOf('/') + 1);
        }
        return null;
    }

    /**
     * Multipart 의 OriginalFilename 값에서 . 이후 확장자만 잘라냅니다.
     * @param OriginalFilename ex) myFile.png
     * @return ex) png
     */
    public static String getFormatByName(String OriginalFilename) {
        if (StringUtils.hasText(OriginalFilename)) {
            return OriginalFilename.substring(OriginalFilename.lastIndexOf('.') + 1);
        }
        return null;
    }

    /**
     * 파일의 전체 경로를 생성합니다.
     * @param fileId 생성된 파일 고유 ID
     * @param format 확장자
     */
    public static String createPath(String fileId, String format) {
        return String.format("%s/%s.%s", BASE_DIR, fileId, format);
    }
}
```
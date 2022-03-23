package org.fytyny.dirdrive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.fytyny.dirdrive.api.dto.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JsoupDownloadIT {

    private final String X_API_KEY = "X-api-key";
    private final String X_SESSION_TOKEN = "X-session-token";
    private final String API_KEY = "superapikey";

    private final String PORT = "8080";
    private final String HOST = "http://localhost:";
    private final String MODULE_NAME = "DD";
    private final String CREATE_SESSION = "/" + MODULE_NAME + "/rest/session/create";
    private final String GET_DIRS = "/" + MODULE_NAME + "/rest/dir/listAll";
    private final String GET_FILE_LIST = "/" + MODULE_NAME + "/rest/dir/get";
    private final String GET_FILE = "/" + MODULE_NAME + "/rest/dir/get/file";

    ObjectMapper objectMapper = new ObjectMapper();

    TypeReference typeReference = new TypeReference<Map<String,Object>>(){};
    @Test
    public void test() throws IOException{
        String token = getToken();
        log.info(token);
        DirectoryDTO musicDir = getMusicDir(token);
        List<FileDTO> fileList = getFileList(token, musicDir);
        log.info(fileList.toString());
        Random random = new Random();
        int i = random.nextInt(fileList.size());
        FileRequest fileRequest = new FileRequest();
        fileRequest.setDirectoryDTO(musicDir);
        FileDTO fileDTO = fileList.get(i);
        fileRequest.setFileDTO(fileDTO);
        downloadFile(token,fileRequest);
        File file = new File(fileDTO.getName());
        Assertions.assertTrue(file.exists());
    }

    private boolean downloadFile(String sesionToken, FileRequest request) throws IOException {
        String url = HOST + PORT + GET_FILE;
        String requestBody = objectMapper.writeValueAsString(request);
        Connection.Response post = post(url, requestBody, getHeaders().putDsl(X_SESSION_TOKEN, sesionToken));
        FileOutputStream out = (new FileOutputStream(new java.io.File(request.getFileDTO().getName())));
        BufferedInputStream bufferedInputStream = post.bodyStream();
        byte[] b = new byte[1024];
        int d;
        while ((d = bufferedInputStream.read(b)) != -1){
            out.write(b);

        }
        out.close();
        return true;
    }

   @Test
    public void tesst() throws IOException {
       String url = "http://localhost:8080/DD/rest/test/download";
       byte[] bytes = get(url, getHeaders()).bodyAsBytes();
        FileOutputStream out = (new FileOutputStream(new java.io.File("download.mp3")));
        //FileUtils.copyURLToFile(new URL(url), new File("down.mp3"));
        out.write(bytes);
        out.close();

    }

    @SuppressWarnings("unchecked")
    List<DirectoryDTO> getDirectories(String sessionToken) throws IOException {
        String url = HOST + PORT + GET_DIRS;
        String body = get(url, getHeaders().putDsl(X_SESSION_TOKEN,sessionToken)).body();
        List l = (List) ((Map) objectMapper.readValue(body,typeReference)).get("directoryList");
        Object collect = l.stream().map(g -> {
            DirectoryDTO dto = new DirectoryDTO();
            dto.setPath(((Map)g).get("path").toString());
            dto.setLabel(((Map)g).get("label").toString());
            return dto;
        }).collect(Collectors.toList());
        return (List) collect;
    }

    @SuppressWarnings("unchecked")
    List<FileDTO> getFileList(String sessionToken, DirectoryDTO directoryDTO) throws IOException {
        String url = HOST + PORT + GET_FILE_LIST;
        String req = objectMapper.writeValueAsString(directoryDTO);
        String body = post(url,req,getHeaders().putDsl(X_SESSION_TOKEN,sessionToken)).body();
        List l = (List) ((Map) objectMapper.readValue(body,typeReference)).get("fileDTOList");
        Object collect = l.stream().map(g -> {
            FileDTO dto = new FileDTO();
            dto.setModifyDate(((Map)g).get("modifyDate").toString());
            dto.setName(((Map)g).get("name").toString());
            return dto;
        }).collect(Collectors.toList());
        return (List) collect;
    }

    DirectoryDTO getMusicDir(String sessionToken) throws IOException{
        Optional<DirectoryDTO> music = getDirectories(sessionToken).stream().filter(f -> f.getLabel().equals("MUSIC")).findAny();
        return music.get();
    }

    private String getToken()  throws IOException{
        String url = HOST + PORT + CREATE_SESSION;

        Connection.Response execute = get(url,getHeaders());
        Map o = (Map)objectMapper.readValue(execute.body(), typeReference);

        return o.get("token").toString();
    }

    private Connection.Response get(String url, Map<String,String> headers){
        try {
            return Jsoup.connect(url).maxBodySize(Integer.MAX_VALUE).headers(headers).ignoreContentType(true).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    private Connection.Response post(String url, String body,Map<String,String> headers){
        try {
            return Jsoup.connect(url).method(Connection.Method.POST).maxBodySize(Integer.MAX_VALUE).headers(headers).requestBody(body).ignoreContentType(true).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }
    HashDslMap<String,String> getHeaders(){
        HashDslMap<String,String> headers = new HashDslMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON);
        headers.put(X_API_KEY,API_KEY);
        return headers;
    }

    static class HashDslMap<K,V> extends HashMap<K,V>{
        HashDslMap<K,V> putDsl(K key, V value){
            super.put(key,value);
            return this;
        }
    }

}

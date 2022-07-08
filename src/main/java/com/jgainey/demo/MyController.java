package com.jgainey.demo;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

@Controller
public class MyController {

    boolean fileGenerated = false;

    @RequestMapping(method = RequestMethod.GET, value = "/generate-file", produces = "application/json")
    public ResponseEntity<String> generateFile(@RequestParam(value = "bytesize", defaultValue = "1000") int sizeInBytes){
        if(!fileGenerated){
            final ByteBuffer buf = ByteBuffer.allocate(4).putInt(2);
            buf.rewind();

            final OpenOption[] options = { StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW , StandardOpenOption.SPARSE };
            final Path hugeFile = Paths.get("/tmp/hugefile.txt");

            try (final SeekableByteChannel channel = Files.newByteChannel(hugeFile, options);) {
                channel.position(sizeInBytes);
                channel.write(buf);
                fileGenerated=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ResponseEntity<>("File Generated", HttpStatus.OK);
    }


    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {

        if(fileGenerated){
            InputStream inputStream = null; //load the file
            response.setHeader("Content-Disposition","attachment; filename=hugefile.txt");
            try {
                inputStream = new FileInputStream(new File("/tmp/hugefile.txt"));
                IOUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

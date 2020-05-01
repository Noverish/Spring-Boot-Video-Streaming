package kim.hyunsub.videostreaming.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;

@Controller
@RequestMapping(path = "**", params = "raw")
class FileController {

    @GetMapping()
    public void index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        File file = new File("/archive" + fullPath);

        if (!file.isFile()) {
            throw new Exception("'" +  fullPath  + "' is not file");
        }

        Path path = file.toPath();
        String mimeType = Files.probeContentType(path);
        InputStream is = new FileInputStream(file);

        response.setContentType(mimeType);
        IOUtils.copy(is, response.getOutputStream());
    }
    
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException(Exception e, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.write(e.getMessage());
        writer.close();
    }
}
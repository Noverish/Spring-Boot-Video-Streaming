package kim.hyunsub.videostreaming.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;

@Controller
@RequestMapping(path = "**", params = "raw")
class FileController {

    @GetMapping(path = "**/*.mp4")
    public ResponseEntity<ResourceRegion> video(HttpServletRequest request, @RequestHeader HttpHeaders headers)
            throws Exception {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        File file = new File("/archive" + fullPath);
        if (!file.isFile()) {
            throw new FileController.NotFileException("'" + fullPath + "' is not file");
        }

        FileSystemResource resource = new FileSystemResource(file);
        ResourceRegion region = resourceRegion(resource, headers);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(Files.probeContentType(Paths.get(fullPath)))).body(region);
    }

    private ResourceRegion resourceRegion(FileSystemResource video, HttpHeaders headers) throws IOException {
        long contentLength = video.contentLength();
        List<HttpRange> ranges = headers.getRange();
        if (ranges.size() > 0) {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Long.min(contentLength, end - start + 1);
            return new ResourceRegion(video, start, rangeLength);
        } else {
            return new ResourceRegion(video, 0, contentLength);
        }
    }

    @GetMapping()
    public void index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        File file = new File("/archive" + fullPath);
        if (!file.isFile()) {
            throw new FileController.NotFileException("'" + fullPath + "' is not file");
        }

        Path path = file.toPath();
        String mimeType = Files.probeContentType(path);
        InputStream is = new FileInputStream(file);

        response.setContentType(mimeType);
        IOUtils.copy(is, response.getOutputStream());
    }

    static class NotFileException extends Exception {
        private static final long serialVersionUID = 8679213056519109896L;

        public NotFileException(String message) {
            super(message);
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException(FileController.NotFileException e, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        e.printStackTrace(writer);
        writer.close();
    }
}
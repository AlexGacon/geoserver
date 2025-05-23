/*
 *  (c) 2019 Open Source Geospatial Foundation - all rights reserved
 *  This code is licensed under the GPL 2.0 license, available at the root
 *  application directory.
 */
package org.geoserver.ogcapi;

import javax.servlet.http.HttpServletResponse;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.impl.ServiceInfoImpl;
import org.geoserver.ows.HttpErrorCodeException;
import org.geoserver.platform.ServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@APIService(
        service = "Hello",
        version = "1.0.1",
        landingPage = "ogc/hello/v1",
        serviceClass = HelloService.HelloServiceInfo.class)
@RequestMapping(path = APIDispatcher.ROOT_PATH + "/hello/v1")
public class HelloService {

    public static final String DEFAULT_GREETING = "hello";
    private final HelloServiceInfoImpl serviceInfo;

    static interface HelloServiceInfo extends ServiceInfo {}

    static class HelloServiceInfoImpl extends ServiceInfoImpl implements HelloServiceInfo {}

    String defaultValue = DEFAULT_GREETING;

    public HelloService() {
        this.serviceInfo = new HelloServiceInfoImpl();
        this.serviceInfo.setName("Hello");
        this.serviceInfo.setTitle("Hello Service");
        this.serviceInfo.setEnabled(true);
    }

    @GetMapping(name = "landingPage")
    @ResponseBody
    public Message landingPage() {
        return new Message("Landing page");
    }

    @GetMapping(path = DEFAULT_GREETING, name = "sayHello")
    @ResponseBody
    public Message hello(@RequestParam(name = "message", required = false) String message) {
        return new Message(message != null ? message : defaultValue);
    }

    @PostMapping(path = "echo")
    @ResponseBody
    public ResponseEntity echo(@RequestBody Message message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(message.getMessage(), headers, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "delete")
    @ResponseBody
    public ResponseEntity<String> delete() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "default")
    public void putDefault(@RequestBody String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @GetMapping(path = "noContent")
    public void httpErrorCodeException() {
        throw new HttpErrorCodeException(HttpServletResponse.SC_NO_CONTENT);
    }

    @GetMapping(path = "wrappedException")
    public void wrappedHttpErrorCodeException() {
        try {
            throw new HttpErrorCodeException(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            throw new ServiceException("Wrapping code error", e);
        }
    }

    @GetMapping(path = "badRequest")
    public void badRequestHttpErrorCodeException() {
        throw new HttpErrorCodeException(HttpServletResponse.SC_BAD_REQUEST);
    }

    @GetMapping(path = "errorWithPayload")
    public void httpErrorCodeExceptionWithContentType() {
        throw new HttpErrorCodeException(HttpServletResponse.SC_OK, "{\"hello\":\"world\"}")
                .setContentType("application/json");
    }

    @GetMapping(path = "document", name = "document")
    @ResponseBody
    @HTMLResponseBody(templateName = "message.ftl", fileName = "message.html")
    public HelloDocument document() {
        HelloDocument doc = new HelloDocument();
        doc.setMessage(defaultValue);
        doc.addSelfLinks("ogc/hello/v1/document");
        return doc;
    }

    /** Used by the {@link org.geoserver.ows.DisabledServiceCheck} */
    public HelloServiceInfo getServiceInfo() {
        return serviceInfo;
    }
}

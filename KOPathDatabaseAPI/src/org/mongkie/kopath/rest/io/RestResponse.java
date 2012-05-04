/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 * 
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.kopath.rest.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class RestResponse {

    private ByteArrayOutputStream os;
    private String contentType = "text/plain";
    private String contentEncoding;
    private int responseCode;
    private String responseMsg;
    private long lastModified;

    public RestResponse() {
        os = new ByteArrayOutputStream();
    }

    public RestResponse(byte[] bytes) throws IOException {
        this();

        byte[] buffer = new byte[1024];
        int count = 0;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        while ((count = bis.read(buffer)) != -1) {
            write(buffer, 0, count);
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public void setResponseMessage(String msg) {
        this.responseMsg = msg;
    }

    public String getResponseMessage() {
        return responseMsg;
    }

    public void setResponseCode(int code) {
        this.responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void write(byte[] bytes, int start, int length) {
        os.write(bytes, start, length);
    }

    public byte[] getDataAsByteArray() {
        return os.toByteArray();
    }

    public String getDataAsString() {
        try {
            return os.toString("UTF-8");
        } catch (Exception ex) {
            Logger.getLogger(RestConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public OutputStream getOutputStream() {
        return os;
    }

    public <T> T getDataAsObject(Class<T> jaxbClass) throws JAXBException {
        return getDataAsObject(jaxbClass, jaxbClass.getPackage().getName());
    }

    @SuppressWarnings("unchecked")
    public <T> T getDataAsObject(Class<T> clazz, String packageName) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        Object obj = u.unmarshal(new StreamSource(new StringReader(getDataAsString())));

        if (obj instanceof JAXBElement) {
            return (T) ((JAXBElement) obj).getValue();
        } else {
            return (T) obj;
        }
    }
}

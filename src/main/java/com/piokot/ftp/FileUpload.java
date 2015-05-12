/**
 * Copyright (c) 2015, piotrkot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the FreeBSD Project.
 */
package com.piokot.ftp;

import com.piokot.ftp.api.Callback;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;

/**
 * FTP Command for uploading a file.
 *
 * @author Piotr Kotlicki (piotr.kotlicki@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class FileUpload extends AbstractFTPCommand<Boolean> {
    /**
     * Remote destination for upload.
     */
    private final transient String dst;
    /**
     * Local resource to upload.
     */
    private final transient InputStream src;

    /**
     * Class constructor.
     *
     * @param remote Remote destination of the resource.
     * @param local Local resource to upload.
     * @param callback Callback after upload execution.
     */
    public FileUpload(final String remote, final InputStream local,
        final Callback<Boolean> callback) {
        super(callback);
        this.dst = remote;
        this.src = local;
    }

    @Override
    @SneakyThrows
    public Boolean ftpCall(final FTPClient client) {
        return client.storeFile(this.dst, this.src);
    }
}

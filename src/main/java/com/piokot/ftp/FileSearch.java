/**
 * Copyright (c) 2015, Piotr Kotlicki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.piokot.ftp;

import com.google.common.base.Joiner;
import com.piokot.ftp.api.Callback;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.Collection;

/**
 * FTP Command for searching (also recursively) files with given prefix.
 */
public class FileSearch extends FTPCommand<Iterable<String>> {
    private final String dir;
    private final String prefix;
    private final boolean recurs;

    public FileSearch(final String directory, final String filePrefix, final boolean recursive,
                      final Callback<Iterable<String>> callback) {
        super(callback);
        this.dir = directory;
        this.prefix = filePrefix;
        this.recurs = recursive;
    }

    @Override
    protected Iterable<String> ftpCall(final FTPClient ftpClient) {
        return search(new ArrayList<String>(), this.dir, ftpClient);
    }

    @SneakyThrows
    private Collection<String> search(final Collection<String> result, final String directory,
                                              final FTPClient ftpClient) {
        final FTPFile[] ftpFiles = ftpClient.listFiles(directory);
        for (FTPFile ftpFile : ftpFiles) {
            if (ftpFile.isFile() && ftpFile.getName().startsWith(this.prefix)) {
                result.add(Joiner.on("/").join(directory, ftpFile.getName()));
            } else if (ftpFile.isDirectory() && this.recurs) {
                search(result,
                    Joiner.on("/").join(directory, ftpFile.getName()),
                    ftpClient);
            }
        }
        return result;
    }
}

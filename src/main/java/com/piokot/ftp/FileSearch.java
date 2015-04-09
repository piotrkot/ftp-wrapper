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

import com.google.common.base.Joiner;
import com.piokot.ftp.api.Callback;
import com.piokot.ftp.api.Filter;
import java.util.ArrayList;
import java.util.Collection;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP Command for searching (also recursively) files with given prefix.
 *
 * @author Piotr Kotlicki (piotr.kotlicki@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class FileSearch extends AbstractFTPCommand<Iterable<String>> {
    /**
     * Directory to start search from.
     */
    private final transient String dir;
    /**
     * Filter for files found.
     */
    private final transient Filter<FTPFile> fltr;
    /**
     * Is search recursive.
     */
    private final transient boolean recurs;

    /**
     * Class constructor.
     *
     * @param directory Directory to start search from.
     * @param filter Limiting files found to those filtered.
     * @param recursive Is search recursive.
     * @param callback Callback on files found.
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public FileSearch(
        final String directory,
        final Filter<FTPFile> filter,
        final boolean recursive,
        final Callback<Iterable<String>> callback
    ) {
        super(callback);
        this.dir = directory;
        this.fltr = filter;
        this.recurs = recursive;
    }

    @Override
    public Iterable<String> ftpCall(final FTPClient client) {
        return this.search(new ArrayList<String>(0), this.dir, client);
    }

    /**
     * Recursive search in directory calling FTP client search.
     *
     * @param result Partial results for recursion.
     * @param directory Directory to be searched in.
     * @param client Apache FTP client.
     * @return File names found.
     */
    @SneakyThrows
    private Collection<String> search(
        final Collection<String> result,
        final String directory,
        final FTPClient client
    ) {
        final String sep = "/";
        final FTPFile[] ftpFiles = client.listFiles(directory);
        for (final FTPFile ftpFile : ftpFiles) {
            if (ftpFile.isFile() && this.fltr.valid(ftpFile)) {
                result.add(Joiner.on(sep).join(directory, ftpFile.getName()));
            } else if (ftpFile.isDirectory() && this.recurs) {
                this.search(
                    result,
                    Joiner.on(sep).join(directory, ftpFile.getName()),
                    client
                );
            }
        }
        return result;
    }
}

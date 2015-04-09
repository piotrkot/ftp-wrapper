Simple object oriented wrapper for Apache FTPClient.

Using [Apache FTPClient](https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html)
we are forced to control the state. In a typical example we do:

```
FTPClient f = new FTPClient();
f.connect(server, port);
f.login(username, password);
FTPFile[] files = f.listFiles(directory);
// display files' info
f.logout();
f.disconnect();
```

Apart from the need to handle checked `SocketException` and `IOException`,
we must know and remember what is our state of connection. There is no
responsibility on the FTPClient object. It doesn't act according to its
predefined behaviour. Simply because it has no behaviour.

All it does it gives us list of methods to be called with control hand over
to developer. Developer writing procedures which call methods define
the behaviour. This is Procedural Programming (I call it Method Oriented
Programming - MOP).

When the logic gets complicated, the procedure grows. Then it starts being
too big for a single method or file and is split and organized into smaller
procedures. Eventually, we end up having many utility classes 
(sometimes even static methods) providing us well-described methods. Nothing
becomes encapsulated as the code explicitly shows what and when is done. But
there becomes lots of code to comprehend and read.

Instead, we would like to have OOP, with clearly defined objects, their
behaviour and responsibility. Basically, the SOLID principles.

Here is how FTP wrapper lists directory:

```
new FTP(server, port, username, password).onConnect(
    new DirList(
        directory,
        new Callback<FTPFile[]>() {
            public void onReturn(final FTPFile[] files) {
            // display files' info
            }
        }
    )
);
```

Although there is more lines of code and it may seam complex, we know
exactly what the flow is. There is no need to check state as there is no. There
is defined process and we can only fill in the implementation details. Indeed,
it is the restriction on developer but only to guarantee sound design and
maintenance in the long run.

Here are more examples on how to use it.

Upload a file and download afterwards:

```
new FTP(server, port, username, password).onConnect(
    new FileUpload(
        "dir/file",
        new ByteArrayInputStream("content".getBytes(Charsets.UTF_8)),
        new Callback<Boolean>() {
            public void onReturn(final Boolean boo) {
                Assert.assertTrue("File not uploaded", boo);
            }
        }
    ),
    new FileDownload(
        "dir/file",
        new Callback<InputStream>() {
            @SneakyThrows
            public void onReturn(final InputStream input) {
                Assert.assertEquals(
                    "content",
                    CharStreams.toString(
                        new InputStreamReader(input, Charsets.UTF_8)
                    )
                );
            }
        }
    )
);
```

Upload a file, delete and check if exists:

```
new FTP(server, port, username, password).onConnect(
    new FileUpload(
        "dir/file",
        new ByteArrayInputStream("content".getBytes(Charsets.UTF_8)),
        new Callback<Boolean>() {
            public void onReturn(final Boolean boo) {
                Assert.assertTrue("File not uploaded", boo);
            }
        }
    ),
    new FileDelete(
        "dir/file",
        new Callback<Boolean>() {
            public void onReturn(final Boolean boo) {
                Assert.assertTrue("File not deleted", boo);
            }
        }
    ),
    new DirList(
        "dir",
        new Callback<FTPFile[]>() {
            public void onReturn(final FTPFile[] files) {
                for (final FTPFile file : files) {
                    if ("file".equals(file.getName())) {
                        Assert.fail("File exists");
                    }
                }
            }
        }
    )
);
```

Download as a zip all files found recursively with given prefix:

```
new FTP(server, port, username, password).onConnect(
    new FileSearch("dir", new Prefix("prefix"), true, new Zip())
);
class Prefix implements Filter<FTPFile> {
    private final transient String prfx;
    Prefix(final String prefix) {
        this.prfx = prefix;
    }
    @Override
    public boolean valid(final FTPFile file) {
        return file.getName().startsWith(this.prfx);
    }
}
class Zip implements Callback<Iterable<String>> {
    @Override
    @SneakyThrows
    public void onReturn(final Iterable<String> findings) {
        final ZipOutputStream zip = new ZipOutputStream(
            new BufferedOutputStream(new FileOutputStream("found.zip"))
        );
        for (final String found : findings) {
            zip.putNextEntry(new ZipEntry(found));
            new FileDownload(
                found,
                new Callback<InputStream>() {
                    @SneakyThrows
                    public void onReturn(final InputStream stream) {
                        ByteStreams.copy(stream, zip);
                        stream.close();
                    }
                });
        }
        zip.close();
    }
}
```


Feel free to fork me on GitHub, report bugs or post comments.
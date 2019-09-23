/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yolanda.nohttp;

import com.yolanda.nohttp.able.Cancelable;

import java.io.OutputStream;

/**
 * <p>File interface.
 * All the methods are called in Son thread.
 * 文件接口。
 * *所有子线程的方法。</p>
 * Created in Oct 12, 2015 4:44:07 PM.
 *
 * @author Yan Zhenjie.
 */
public interface Binary extends Cancelable {

    /**
     * Returns the size of the Binary, if size is 0, the Binary Field will not be sent. The rest of the {@link Binary} method will not be invoked.
     *返回二进制文件的大小,如果大小为0,二进制字段将不会被发送。其余的{@link二进制}方法将不会被调用。
     * @return Long length.
     */
    long getLength();

    /**
     * Write your Binary data through flow out.
     *通过flow输出二进制数据。
     * @param outputStream {@link OutputStream}.
     */
    void onWriteBinary(OutputStream outputStream);

    /**
     * Return the fileName, Can be null.
     *
     * @return File name.
     */
    String getFileName();

    /**
     * Return mimeType of binary.
     *
     * @return MimeType.
     */
    String getMimeType();
}

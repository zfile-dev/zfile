/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.zhaojun.zfile.core.io;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

/**
 * 
 * 自定义 EnsureContentLengthInputStreamResource 可以保证必须实现 InputStream 的 contentLength 方法返回实际的长度.
 * 此类相较于 {@link org.springframework.core.io.InputStreamResource} 仅实现了 contentLength 方法.
 * <br><br>
 * {@link org.springframework.core.io.Resource} implementation for a given {@link InputStream}.
 * <p>Should only be used if no other specific {@code Resource} implementation
 * is applicable. In particular, prefer {@link ByteArrayResource} or any of the
 * file-based {@code Resource} implementations where possible.
 *
 * <p>In contrast to other {@code Resource} implementations, this is a descriptor
 * for an <i>already opened</i> resource - therefore returning {@code true} from
 * {@link #isOpen()}. Do not use an {@code InputStreamResource} if you need to
 * keep the resource descriptor somewhere, or if you need to read from a stream
 * multiple times.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 28.12.2003
 * @see ByteArrayResource
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.FileSystemResource
 * @see org.springframework.core.io.UrlResource
 */
public class EnsureContentLengthInputStreamResource extends InputStreamResource {

	private final long contentLength;

	/**
	 * Create a new InputStreamResource.
	 * @param inputStream the InputStream to use
	 */
	public EnsureContentLengthInputStreamResource(InputStream inputStream, long contentLength) {
        super(inputStream);
        this.contentLength = contentLength;
	}

	@Override
	public long contentLength() {
		return contentLength;
	}

}
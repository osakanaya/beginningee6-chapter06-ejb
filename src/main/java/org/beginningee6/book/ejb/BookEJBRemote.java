package org.beginningee6.book.ejb;

import java.util.List;

import javax.ejb.Remote;

import org.beginningee6.book.chapter06.jpa.Book;

/**
 * Beanクラス：BookEJBのリモートインタフェース。
 * 
 * リモート呼び出しでEJBにアクセスするクライアントへ
 * 公開するメソッドを定義する。
 * 
 * リモートインタフェースとして機能させるには、
 * ＠Remoteアノテーションを付加する必要がある。
 * 
 * BookEJBRemoteインターフェースは＠Remoteアノテーションにより、
 * リモートインターフェースに指定されているため、
 * Beanクラス：BookEJBRemoteには＠Remoteアノテーションを指定する必要はない。
 * 
 */
@Remote		// リモートインターフェースの指定
public interface BookEJBRemote {
	public List<Book> findBooks();
	public Book findBookById(Long id);
	public Book createBook(Book book);
	public void deleteBook(Book book);
	public Book updateBook(Book book);
}

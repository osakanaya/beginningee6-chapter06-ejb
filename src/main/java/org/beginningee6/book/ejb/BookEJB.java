package org.beginningee6.book.ejb;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.beginningee6.book.chapter06.jpa.Book;

/**
 * ＠Statelessアノテーションにより、
 * このBeanクラスがステートレス・セッションBeanとして
 * 機能するよう指定している。
 * 
 * ＠LocalBeanでこのBeanクラスのすべてのpublicメソッドを
 * ローカルインタフェースとして公開している。
 * 
 * また、リモートインタフェースとして定義されている
 * BookEJBRemoteインターフェースをこのBeanクラスで実装し、
 * リモート呼び出し用に公開している。
 * 
 * BookEJBRemoteインターフェースは＠Remoteアノテーションにより、
 * リモートインターフェースに指定されているため、
 * このBeanクラスには＠Remoteアノテーションを指定する必要はない。
 * 
 * BookEJBRemoteインタフェースに＠Remoteアノテーションが指定されて
 * いない場合は、このBeanクラスで＠Remoteアノテーションを付加し、
 * インターフェースクラスとしてBookEJBRemoteインタフェースを指定する。
 * 
 * ローカルインタフェースおよびリモートインタフェースとして公開された
 * メソッドが開始されるとトランザクションが開始され、メソッドの処理が
 * 終了するとトランザクションがコミットされる。
 * 
 */
@Stateless
//@Remote(BookEJBRemote.class)　// ＠Remoteアノテーションを利用して
								// BookEJBRemoteインタフェースリモートインターフェース
								// として定義していない場合は、これが必要
@LocalBean
public class BookEJB implements BookEJBRemote {
	
	// ＠PersistenceContextによりEJBコンテナによって
	// 注入されるエンティティマネージャ
	// 永続性ユニットに"Chapter06ProductionPU"を指定
	// （プロジェクト：beginningee6-chapter06-jpaのpersistense.xmlに
	// 定義されている）
	@PersistenceContext(unitName = "Chapter06ProductionPU")
	private EntityManager em;
	
	/**
	 * すべてのBookエンティティを取得（JPAによるデータベース検索）
	 */
	public List<Book> findBooks() {
		
		// 名前付きクエリを作成
		TypedQuery<Book> query = em.createNamedQuery("findAllBooks", Book.class);
		
		// クエリを実行して結果を返す
		return query.getResultList();
	}

	/**
	 * id指定でBookエンティティを取得（JPAによるデータベース検索）
	 */
	public Book findBookById(Long id) {
		
		// EntityManager.find()を使用
		return em.find(Book.class, id);
	}

	/**
	 * Bookエンティティを永続化（JPAによるレコード挿入）
	 */
	public Book createBook(Book book) {
		
		// データベースへ登録
		em.persist(book);
		
		return book;
	}

	/**
	 * Bookエンティティを削除（JPAによるレコード削除）
	 */
	public void deleteBook(Book book) {
		
		// 削除するエンティティをEntity Managerの管理下に置く
		book = em.merge(book);
		
		// データベースから削除
		em.remove(book);
	}

	/**
	 * Bookエンティティを更新（JPAによるデータベース更新）
	 */
	public Book updateBook(Book book) {

		// 引数として受け取るBookエンティティのフィールド値で
		// マッピングされるデータベースレコードを更新
		// （このメソッドの終了時にコミットされてデータベースが更新される）
		return em.merge(book);
	}

}

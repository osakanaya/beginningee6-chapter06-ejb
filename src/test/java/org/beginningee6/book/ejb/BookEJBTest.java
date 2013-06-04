package org.beginningee6.book.ejb;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.beginningee6.book.chapter06.jpa.Book;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ステートレス・セッションBeanのテスト。
 * 
 * このテストでは、ローカル呼び出しにより
 * ローカルインタフェースとして公開された
 * すべてのメソッドを実行してその結果を検証している。
 * 
 * ステートレス・セッションBeanとして定義された
 * BookEJBクラスのオブジェクトは、＠EJBアノテーションの
 * 機能を利用してアプリケーションサーバにより自動的に
 * 注入される。
 * 
 * テストメソッド（つまり、ローカル呼び出しにおける
 * EJBのクライアント）は、対象がEJBであるかどうかを
 * 意識することなく、通常と同様のメソッド実行と同じ
 * 方法で注入されたBookEJBクラスのオブジェクトに対して
 * メソッド実行を行うことで、EJBに処理を行わせることが
 * できる。
 * 
 */
@RunWith(Arquillian.class)
public class BookEJBTest {
	
	private static final Logger logger = Logger.getLogger(BookEJBTest.class
			.getName());

	/**
	 * Arquillianの＠Deploymentアノテーションを付加して
	 * デプロイ動作を指定する。
	 * 
	 * ShrinkWrapを使用してパッケージング（WARの作成）を行い、
	 * テスト時にデプロイすべきアーカイブとして返す。
	 * 
	 * このメソッドによりWARアーカイブが作成されると、
	 * ArquillianによってこのアーカイブがJBossへデプロイされ、
	 * テストメソッドが実行される。
	 * 
	 * テストが終了すると、ArquillianによってこのWARアーカイブが
	 * 自動的にアンデプロイされる。
	 * 
	 */
	@Deployment
	public static Archive<?> createDeployment() {

		// このプロジェクトで作成するEJBの動作にはJPAライブラリ
		// （beginningee6-chapter06-jpa）のデータ永続化機能を
		// 使用しているため、このJPAライブラリをパッケージに含める
		// 必要がある。
		// ここでは、Mavenの依存関係を利用してJPAライブラリを
		// 取得するようにしている。
		File[] dependencyLibs 
			= Maven
				.configureResolver()				
				// Mavenの設定ファイル（settings.xml）のパスを指定（パスは適切に書き換える）
				.fromFile("D:\\apache-maven-3.0.3\\conf\\settings.xml")
//				.fromFile("C:\\Maven\\apache-maven-3.0.5\\conf\\settings.xml")
				// 取得するJPAライブラリのグループID・アーティファクトID・バージョン
				// を指定する。
				//
				// テスト実行にあたり、プロジェクト：beginningee6-chapter06-jpa の
				// プロジェクトで"mvn clean install"コマンドを実行して
				// MavenのローカルリポジトリにこのJPAライブラリをインストールしておく必要がある
				.resolve("org.beginningee6.book:beginningee6-chapter06-jpa:0.0.1-SNAPSHOT")
				.withTransitivity()
				.asFile();

		// JBossにデプロイするWARパッケージを作成する。
		// 
		// このWARパッケージには、以下のものを含める。
		// 
		// １．テスト対象のEJB（BookEJB、BookEJBRemote）
		// ２．テストコード（BookEJBTest）
		// ３．テスト対象のEJBが依存するJPAライブラリ（beginningee6-chapter06-jpa）
		// ４．JPAライブラリで必要となるJDBCデータソース定義ファイル（jbossas-ds.xml）
		// ５．CDI設定ファイル（beans.xml：ただし、空のファイル）
		// 　　⇒＠PersistenceContext、＠Inject、＠EJBアノテーションにより、
		// 　　　EntityManager、UserTransaction、テスト対象のEJBを注入する必要があるため）
		// 
		// なお、上記３．で他のJARファイルを含めてEJB等をパッケージングする必要があるため、
		// JARパッケージではなく、WARパッケージまたはEARパッケージとしてパッケージングする
		// 必要がある。
		// JavaEE6からEJB-Liteというプロファイルが導入され、簡易なEJBであればWARパッケージ
		// としてデプロイすれば動作するようになったため、今回のテストでも上記１～５の
		// ファイルをWARパッケージとしてパッケージングすることにしている。
		WebArchive archive = ShrinkWrap
				// WebArchive（WAR）としてアーカイブを作成
				.create(WebArchive.class)
				// テスト対象のEJB、テストコードを含める
				.addPackage(BookEJB.class.getPackage())
				// JPAライブラリを含める
				.addAsLibraries(dependencyLibs)
				// JPAライブラリが必要とするデータソース定義ファイルを含める
				.addAsWebInfResource("jbossas-ds.xml")
				// CDI設定ファイルを含める
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return archive;
	}

	// 通常、EJBからJPAによりデータアクセスを行う場合は、EntityManagerがEJBに
	// 注入され、かつ、トランザクションの開始と終了はEJBコンテナにより自動的に制御
	// される。
	// 
	// テストデータの登録や削除は、デプロイされるJPAライブラリを用いて直接データベース
	// にアクセスして行うため、この用途だけのためにEntityManagerやUserTransactionを
	// 注入する。
	
	@PersistenceContext					// テストデータ登録・削除用に必要となる
	EntityManager em;					// EntityManagerを注入

	@Inject								// テストデータ登録・削除用に必要となる
	UserTransaction userTransaction;	// UserTransactionを注入

	@EJB						// ＠EJBアノテーションを指定して、
	BookEJB bookEJB;			// テスト対象のEJBのオブジェクトを注入する
								// この場合、ローカルインタフェースを介して
								// EJBをテストしたことになる。
	
	//@EJB						// リモートインターフェースを介したEJBの
	//BookEJBRemote bookEJB;	// テストを行う場合は、リモートインタフェースを
								// フィールドとして宣言する

	@Before
	public void setUp() throws Exception {
		clearData();
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();

		logger.info("Dumping old records...");

		em.createQuery("DELETE FROM Book").executeUpdate();
		userTransaction.commit();
	}

	/**
	 * 
	 * BookEJB.createBook()のテスト。
	 * 
	 * このメソッドを実行した後、以下の２つの観点から、
	 * Bookエンティティがデータベースに登録されていることを確認している。
	 * 
	 * １．BookEJB.createBook()により返されるBookエンティティでIDが
	 * 　　付番されていること。
	 * ２．付番されたIDをキーにして直接Entity Manager（データベース）から
	 * 　　Bookエンティティを取得し、BookEJB.createBook()の引数に指定した
	 * 　　データを持つこと。
	 * 　　
	 */
	@Test
	public void testCreateABook() throws Exception {
		
		///// 準備 /////
		
		Book book = new Book();
		book.setTitle("The Hitchhiker's Guide to the Galaxy");
		book.setPrice(12.5F);
		book.setDescription("Science fiction comedy book");
		book.setIsbn("1-84023-742-2");
		book.setNbOfPage(354);
		book.setIllustrations(false);

        ///// テスト /////
        
		// EJBオブジェクトを通じてエンティティを永続化
		// （データベースへ登録）
		Book persisted = bookEJB.createBook(book);
        
        ///// 検証 /////
        
		assertThat(persisted.getId(), is(notNullValue()));

		// EJBによりBookエンティティが永続化されたことを確認
		Book found = em.find(Book.class, persisted.getId());
		
		assertThat(found.getId(), is(persisted.getId()));
		assertThat(found.getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
	}
	
	/**
	 * 
	 * BookEJB.findBookById()のテスト。
	 * 
	 * BookEJB.createBook()により取得対象のBookエンティティを永続化した後、
	 * IDをキーにしてEJB経由でBookエンティティのデータをデータベースから
	 * 取り出している。
	 * 
	 */
	@Test
	public void testFindABook() throws Exception {
		
		///// 準備 /////
		
		Book book = new Book();
		book.setTitle("The Hitchhiker's Guide to the Galaxy");
		book.setPrice(12.5F);
		book.setDescription("Science fiction comedy book");
		book.setIsbn("1-84023-742-2");
		book.setNbOfPage(354);
		book.setIllustrations(false);

		// EJBオブジェクトを通じてエンティティを永続化
		// （データベースへ登録）
		book = bookEJB.createBook(book);

        ///// テスト /////
        
		// EJBオブジェクトを通じてエンティティを取得
		// （データベースを検索）
		Book found = bookEJB.findBookById(book.getId());
        
        ///// 検証 /////

		// 永続化したものと同じIDであることを確認
		assertThat(found.getId(), is(book.getId()));
		// 永続化したものと同じtitleであることを確認
		assertThat(found.getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
	}
	
	/**
	 * 
	 * BookEJB.findBooks()のテスト。
	 * 
	 * BookEJB.createBook()により取得対象のBookエンティティを永続化した後、
	 * EJB経由ですべてのBookエンティティのデータをデータベースから
	 * 取り出している。
	 * 
	 */
	@Test
	public void testFindBooks() throws Exception {
		
		///// 準備 /////
		
		Book book = new Book();
		book.setTitle("The Hitchhiker's Guide to the Galaxy");
		book.setPrice(12.5F);
		book.setDescription("Science fiction comedy book");
		book.setIsbn("1-84023-742-2");
		book.setNbOfPage(354);
		book.setIllustrations(false);

		// EJBオブジェクトを通じてエンティティを永続化
		// （データベースへ登録）
		book = bookEJB.createBook(book);

        ///// テスト /////
        
		// EJBオブジェクトを通じて全てのエンティティを取得
		// （データベースを検索）
		List<Book> found = bookEJB.findBooks();
        
        ///// 検証 /////
        
		// エンティティの総数は１
		assertThat(found.size(), is(1));
		// 永続化したものと同じtitleであることを確認
		assertThat(found.get(0).getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
	}
	
	/**
	 * 
	 * BookEJB.deleteBook()のテスト。
	 * 
	 * BookEJB.createBook()により削除対象のBookエンティティを永続化した後、
	 * deleteBook()を実行してこのBookエンティティをデータベースから削除している。
	 * 
	 * Bookエンティティを削除後、削除したBookエンティティのIDをキーにして
	 * findBookById()を実行しても、エンティティが取得されないことで
	 * 削除が正しく行われたことを確認している。
	 * 
	 */
	@Test
	public void testDeleteABook() throws Exception {
		
		///// 準備 /////
		
		Book book = new Book();
		book.setTitle("The Hitchhiker's Guide to the Galaxy");
		book.setPrice(12.5F);
		book.setDescription("Science fiction comedy book");
		book.setIsbn("1-84023-742-2");
		book.setNbOfPage(354);
		book.setIllustrations(false);

		// EJBオブジェクトを通じてエンティティを永続化
		// （データベースへ登録）
		book = bookEJB.createBook(book);

        ///// テスト /////
        
		// EJBオブジェクトを通じて登録したエンティティを削除
		// （データベースから削除）
		bookEJB.deleteBook(book);
        
        ///// 検証 /////
        
		Book found = bookEJB.findBookById(book.getId());
		// エンティティはnull（データベースに存在しない）
		assertThat(found, is(nullValue()));
	}
	
	/**
	 * 
	 * BookEJB.updateBook()のテスト。
	 * 
	 * BookEJB.createBook()により更新対象のBookエンティティを永続化した後、
	 * updateBook()を実行してこのBookエンティティのフィールド値を更新している。
	 * 
	 * updateBook()を実行した後、以下の３つの観点から、
	 * Bookエンティティの更新がデータベースに反映されていることを確認している。
	 * 
	 * １．BookEJB.updateBook()により返されるBookエンティティのIDが永続化時に
	 * 　　付番されたIDと同じであること。
	 * ２．BookEJB.updateBook()により返されるBookエンティティでフィールド値の
	 * 　　更新が反映されていること。
	 * ３．BookエンティティのIDをキーにしてfindBookById()によりBookエンティティ
	 * 　　をデータベースから取得し、取得したエンティティでもフィールド値の更新が
	 * 　　反映されていること。
	 * 
	 */
	@Test
	public void testUpdateABook() throws Exception {
		
		///// 準備 /////
		
		Book book = new Book();
		book.setTitle("The Hitchhiker's Guide to the Galaxy");
		book.setPrice(12.5F);
		book.setDescription("Science fiction comedy book");
		book.setIsbn("1-84023-742-2");
		book.setNbOfPage(354);
		book.setIllustrations(false);

		// EJBオブジェクトを通じてエンティティを永続化
		// （データベースへ登録）
		book = bookEJB.createBook(book);

        ///// テスト /////
        
		// EJBオブジェクトを通じて登録したエンティティの
		// データを変更して、データベースを更新する
		book.setDescription("Updated Description");
		Book updated = bookEJB.updateBook(book);
        
        ///// 検証 /////
        
		// エンティティのデータを検証
		assertThat(updated.getId(), is(book.getId()));
		assertThat(updated.getDescription(), is("Updated Description"));
		
		// EJBオブジェクトを通じてデータベースから
		// 取得したエンティティのデータを検証
		Book found = bookEJB.findBookById(book.getId());
		assertThat(found.getDescription(), is("Updated Description"));
	}
}

package cn.katoumegumi.java.experiment.luence;


/*import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.BaseDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;*/

/**
 * @author ws
 */
public class LuenceTest {


/*    public static void main(String[] args) throws IOException, ParseException {
        create();
        //search();
    }

    public static void search() throws IOException, ParseException {
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        Path path = Paths.get("test");
        Directory directory = FSDirectory.open(path);
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[]{"name"},analyzer);
        QueryParser multiFieldQueryParser = new QueryParser("name",analyzer);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.AND);
        Query query = multiFieldQueryParser.parse("世界");
        System.out.println(query.toString());
        TopDocs topDocs = indexSearcher.search(query,1000);
        //Document document = indexSearcher.doc(1);
        for(ScoreDoc scoreDoc:topDocs.scoreDocs){
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("name"));
            System.out.println(scoreDoc.score+"");
        }
        directory.close();
        indexReader.close();
    }


    public static void create() throws IOException {
        Analyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        Path path = FileSystems.getDefault().getPath("test");
        Directory directory = FSDirectory.open(path);
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        FieldType idType = new FieldType();
        idType.setIndexOptions(IndexOptions.DOCS);
        idType.setStored(true);
        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorOffsets(true);
        for(int i = 0; i < 1000; i++){
            Document document = new Document();
            //TokenStream tokenStream = analyzer.tokenStream("name",("你好世界"+i));
            Field field1 = new Field("id",i+"",idType);
            Field field = new Field("name","你好世界"+i,fieldType);
            document.add(field1);
            document.add(field);
            indexWriter.addDocument(document);
        }
        indexWriter.commit();
        indexWriter.close();
        directory.close();
    }*/


}

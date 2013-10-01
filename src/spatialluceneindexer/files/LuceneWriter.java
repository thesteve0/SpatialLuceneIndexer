/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spatialluceneindexer.files;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import java.io.File;
import java.io.IOException;
import spatialluceneindexer.data.Park;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author spousty
 */
public class LuceneWriter {
    
    private String pathToIndex = "";
    private IndexWriter indexWriter = null;
    private SpatialStrategy spatialStrategy = null;
    private SpatialContext spatialContext = null;
    
    

    private LuceneWriter() {
    }

    public LuceneWriter(String pathToIndex) {
        this.pathToIndex = pathToIndex;
        setUpSpatialPieces();
    }
    
    private void setUpSpatialPieces(){
        //The spatial context is used for creating spatial object. We use GEO because we are using
        //unprojected data on the globe
        this.spatialContext = SpatialContext.GEO;
    
        //this is the spatial indexing strategy we are going to use. By setting maximum 
        // levels to 11 we produce enough specification in the hash to allow for submeter accuracy
        // which may be overkill in this case. 
        SpatialPrefixTree grid = new GeohashPrefixTree(spatialContext, 11);
        //position is the name of the field where you will store the shapes
        this.spatialStrategy = new RecursivePrefixTreeStrategy(grid, "position");
    }
    
    public boolean openIndex(){        
        
        try {
            
            //Open the directory so lucene knows how to deal with it
            Directory dir = FSDirectory.open(new File(pathToIndex));
            
            //Chose the analyzer we are going to use to write documents to the index
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
            
            //Create an index writer configuraiton
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
            
            //we are always going to overwrite the index that is currently in the directory
            iwc.setOpenMode(OpenMode.CREATE);
            
            //let's open that index and get a writer to hand back to the main code
            indexWriter = new IndexWriter(dir, iwc);
            
            return true;
        } catch (Exception e) {
            System.out.println("Threw an exception trying to open the index for writing: " + e.getClass() + " :: " + e.getMessage());
            return false;
        }
                
    }
    
    public void addPark(Park park){
        Document doc = new Document();
        
        doc.add(new TextField("name", park.getname(), Field.Store.YES));
        
        //First we make the shape, then we make the indexed field from it. This field can not be stored
        //This assumes there is always only one shape per document while there could be multiple
        Shape pointShape = spatialContext.makePoint(park.getPos().get(0).doubleValue(), park.getPos().get(1).doubleValue());
        for (IndexableField f : spatialStrategy.createIndexableFields(pointShape)) {
            doc.add(f);
        }
        
        //now let's store the field as well - could be useful to return this to the client
        doc.add(new StoredField("coords", spatialContext.toString(pointShape)));
        
        try {
            indexWriter.addDocument(doc);
        } catch (IOException ex) {
            System.out.println("Threw an exception trying to add the doc: " + ex.getClass() + " :: " + ex.getMessage());
        }
        System.out.println( park.getname() );
        
    }
    
    
    public void finish(){
        System.out.println("about to close the writer");
        try {
            indexWriter.commit();
            indexWriter.close();
        } catch (IOException ex) {
            System.out.println("We had a problem closing the index: " + ex.getClass() + " :: " + ex.getLocalizedMessage());
        }
    }
    
    
    
    
    
}

package org.esa.beam.framework.gpf.main;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.graph.Graph;
import org.esa.beam.framework.gpf.graph.GraphException;
import org.esa.beam.framework.gpf.graph.GraphIO;
import org.esa.beam.framework.gpf.graph.GraphProcessor;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Norman
 * Date: 05.10.2007
 * Time: 17:00:15
 * To change this template use File | Settings | File Templates.
 */
class DefaultCommandLineContext implements CommandLineContext {
    public Product readProduct(String productFilepath) throws IOException {
        Product product;
        product = ProductIO.readProduct(productFilepath, null);
        return product;
    }

    public void writeProduct(Product targetProduct, String filePath, String formatName) throws IOException {
        ProductIO.writeProduct(targetProduct, filePath, formatName);
    }

    public Graph readGraph(String filepath, Map<String, String> parameterMap) throws IOException {
        FileReader fileReader = new FileReader(filepath);
        Graph graph;
        try {
            graph = GraphIO.read(fileReader, parameterMap);
        } finally {
            fileReader.close();
        }
        return graph;
    }

    public void executeGraph(Graph graph) throws GraphException {
        GraphProcessor processor = new GraphProcessor();
        processor.executeGraph(graph, ProgressMonitor.NULL);
    }

    public Map<String, String> readParameterFile(String filepath) throws IOException {
        Properties properties = new Properties();
        FileReader fileReader = new FileReader(filepath);
        HashMap<String, String> hashMap;
        try {
            properties.load(fileReader);
            hashMap = new HashMap<String, String>();
            for (Object object : properties.keySet()) {
                String key = object.toString();
                hashMap.put(key, properties.getProperty(key));
            }
        } finally {
            fileReader.close();
        }
        return hashMap;
    }

    public Product createOpProduct(String opName, Map<String, Object> parameters, Map<String, Product> sourceProducts) throws OperatorException {
        return GPF.createProduct(opName, parameters, sourceProducts);
    }


    public void print(String m) {
        System.out.print(m);
    }
}

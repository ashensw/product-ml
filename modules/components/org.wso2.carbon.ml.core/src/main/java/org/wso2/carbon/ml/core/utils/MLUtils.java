package org.wso2.carbon.ml.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;

public class MLUtils {

    /**
     * Get sequentially picked {@link SamplePoints}
     * @param in data as an input stream.
     * @param dataType type of data CSV, TSV
     * @param sampleSize rows x columns 
     * @return sequentially picked {@link SamplePoints}
     * @throws MLMalformedDatasetException
     */
    public static SamplePoints getSamplePoints(InputStream in, String dataType, int sampleSize)
            throws MLMalformedDatasetException {
        if (in == null ) {
            throw new MLMalformedDatasetException("Failed to parse the given null input stream.");
        }
        Reader reader = new InputStreamReader(in);
        CSVFormat dataFormat = DataTypeFactory.getCSVFormat(dataType);
        CSVParser parser = null;
        try {
            parser = new CSVParser(reader, dataFormat.withHeader().withAllowMissingColumnNames(true));
            Map<String, Integer> headerMap = parser.getHeaderMap();
            int recordsCount = 0;
            CSVRecord row;
            int featureSize = headerMap.size();
            if (sampleSize >= 0) {
                sampleSize = sampleSize / featureSize;
            }
            // List containing actual data of the sample.
            List<List<String>> columnData = new ArrayList<List<String>>();
            
            for (int i=0; i<featureSize; i++) {
                columnData.add(new ArrayList<String>());
            }
            
            Iterator<CSVRecord> datasetIterator = parser.iterator();
            // Count the number of cells contain strings in each column.
            while (datasetIterator.hasNext() && (recordsCount != sampleSize || sampleSize < 0)) {
                row = datasetIterator.next();
                for (int currentCol = 0; currentCol < featureSize; currentCol++) {
                    // Append the cell to the respective column.
                    columnData.get(currentCol).add(row.get(currentCol));
                }
                recordsCount++;
            }
            SamplePoints samplePoints = new SamplePoints();
            samplePoints.setHeader(headerMap);
            samplePoints.setSamplePoints(columnData);
            return samplePoints;
        } catch (Exception e) {
            throw new MLMalformedDatasetException("Failed to parse the given input stream. Cause: "+e, e);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    throw new MLMalformedDatasetException("Failed to close the CSV parser.", e);
                }
            }
        }
    }

    private static class DataTypeFactory {
        static CSVFormat getCSVFormat(String dataType) {
            if ("TSV".equalsIgnoreCase(dataType)) {
                return CSVFormat.TDF;
            }
            return CSVFormat.RFC4180;
        }
    }
}
package decrescendo.evaluate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EvaluateMain {
    private static String benchmarkDirectory;
    private static String dataSetPath;
    private static String outputPath;
    private static List<ClonePair> cloneReferences;
    private static int count;

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (FileInputStream fr = new FileInputStream("./decrescendo.properties")) {
            prop.load(fr);
        }

        benchmarkDirectory = prop.getProperty("benchmarkDirectory");
        if (benchmarkDirectory == null)
            throw new RuntimeException();

        dataSetPath = prop.getProperty("dataSetPath");
        if (dataSetPath == null)
            throw new RuntimeException();
        cloneReferences = getCloneReferences(Paths.get(benchmarkDirectory + dataSetPath));

        outputPath = prop.getProperty("outputPath");
        if (outputPath == null)
            throw new RuntimeException();
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + outputPath);
        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery("select PATH1, START_LINE1, END_LINE1, PATH2, START_LINE2, END_LINE2 from FILE_CLONES");
        executeMatching(rs, false);

        rs = statement.executeQuery("select PATH1, START_LINE1, END_LINE1, PATH2, START_LINE2, END_LINE2 from METHOD_CLONES");
        executeMatching(rs, false);

        rs = statement.executeQuery("select PATH1, START_LINE1, END_LINE1, PATH2, START_LINE2, END_LINE2, GAP_LINE1, GAP_LINE2 from CODEFRAGMENT_CLONES");
        executeMatching(rs, true);

        int cloneCandidateNum = 0;
        rs = statement.executeQuery("select count(*) FROM FILE_CLONES");
        cloneCandidateNum += rs.getInt(1);
        rs = statement.executeQuery("select count(*) FROM METHOD_CLONES");
        cloneCandidateNum += rs.getInt(1);
        rs = statement.executeQuery("select count(*) FROM CODEFRAGMENT_CLONES");
        cloneCandidateNum += rs.getInt(1);

        int matchedCloneReferenceNum = 0;
        for(ClonePair cp : cloneReferences) {
            if(cp.isMatch()) matchedCloneReferenceNum++;
        }

        double recall = (double) matchedCloneReferenceNum / (double) cloneReferences.size();
        double precision = (double) count / (double) cloneCandidateNum;
        double FMeasure = 2 * recall * precision / (recall + precision);
        System.out.println("Recall : " + recall);
        System.out.println("Precision : " + precision);
        System.out.println("F-Measure : " + FMeasure);

        rs.close();
        statement.close();
        connection.close();
    }

    private static void executeMatching(ResultSet rs, boolean existGap) throws SQLException {
        while(rs.next()) {
            String path1 = rs.getString(1);
            int startLine1 = rs.getInt(2);
            int endLine1 = rs.getInt(3);
            List<Integer> gapLines1;
            if(existGap) gapLines1 = getGapLines(rs.getString(7));
            else gapLines1 = null;

            String path2 = rs.getString(4);
            int startLine2 = rs.getInt(5);
            int endLine2 = rs.getInt(6);
            List<Integer> gapLines2;
            if(existGap) gapLines2 = getGapLines(rs.getString(8));
            else gapLines2 = null;

            boolean flag = false;
            for(ClonePair cp : cloneReferences) {
                int okValue;
                int contain1;
                int contain2;

                if(cp.getPath1().equals(path1) && cp.getPath2().equals(path2)){
                    contain1 = getContainValue(startLine1, endLine1, gapLines1, cp.getStartLine1(), cp.getEndLine1(), cp.getGapLine1());
                    contain2 = getContainValue(startLine2, endLine2, gapLines2, cp.getStartLine2(), cp.getEndLine2(), cp.getGapLine2());

                    if(contain1 < contain2) okValue = contain1;
                    else okValue = contain2;
                    if(okValue >= 0.7) {
                        cp.setMatch();
                        flag = true;
                    }
                } else if(cp.getPath1().equals(path2) && cp.getPath2().equals(path1)) {
                    contain1 = getContainValue(startLine1, endLine1, gapLines1, cp.getStartLine2(), cp.getEndLine2(), cp.getGapLine2());
                    contain2 = getContainValue(startLine2, endLine2, gapLines2, cp.getStartLine1(), cp.getEndLine1(), cp.getGapLine1());

                    if(contain1 < contain2) okValue = contain1;
                    else okValue = contain2;
                    if(okValue >= 0.7) {
                        cp.setMatch();
                        flag = true;
                    }
                }
            }
            if(flag) count++;
        }
    }

    private static List<ClonePair> getCloneReferences(Path path) throws IOException {
        List<ClonePair> clonePairList = new ArrayList<>();
        List<String> strs = Files.readAllLines(path);

        for(String str : strs) {
            String[] data = str.split("\t", 0);
            String path1 = benchmarkDirectory + data[2];
            String path2 = benchmarkDirectory + data[5];

            ClonePair cp = new ClonePair(
                    path1.replaceAll("/", "\\\\"),
                    Integer.parseInt(data[3]),
                    Integer.parseInt(data[4]),
                    getGapLines(data[9]),
                    path2.replaceAll("/", "\\\\"),
                    Integer.parseInt(data[6]),
                    Integer.parseInt(data[7]),
                    getGapLines(data[10])
            );

            int gapSize1;
            if(cp.getGapLine1() != null) gapSize1 = cp.getGapLine1().size();
            else gapSize1 = 0;

            int gapSize2;
            if(cp.getGapLine2() != null) gapSize2 = cp.getGapLine2().size();
            else gapSize2 = 0;

            if(cp.getEndLine1() - cp.getStartLine1() + 1 - gapSize1 > 0
                    && cp.getEndLine2() - cp.getStartLine2() + 1 - gapSize2 > 0)
                clonePairList.add(cp);
        }
        return clonePairList;
    }

    private static List<Integer> getGapLines(String str) {
        if(str.equals("") || str.trim().equals("-")) return null;
        List<Integer> gapLines = new ArrayList<>();
        String[] gaps = str.split(",", 0);

        for (String gap : gaps) {
            gapLines.add(Integer.parseInt(gap.trim()));
        }
        return gapLines;
    }

    private static int getContainValue(int startLine1, int endLine1, List<Integer> gapLines1, int startLine2, int endLine2, List<Integer> gapLines2) {
        int  commonLines = 0;

        for(int i = startLine1 ; i <= endLine1 ; i++) {
            if(gapLines1 != null && gapLines1.contains(i)) continue;
            if(gapLines2 != null && gapLines2.contains(i)) continue;
            if(startLine2 <= i && endLine2 >= i) commonLines++;
        }

        int gapSize1;
        if(gapLines1 != null) gapSize1 = gapLines1.size();
        else gapSize1 = 0;

        int gapSize2;
        if(gapLines2 != null) gapSize2 = gapLines2.size();
        else gapSize2 = 0;

        int contain1 = commonLines / (endLine1 - startLine1 + 1 - gapSize1);
        int contain2 = commonLines / (endLine2 - startLine2 + 1 - gapSize2);

        if(contain1 > contain2) return contain1;
        else return contain2;
    }

}

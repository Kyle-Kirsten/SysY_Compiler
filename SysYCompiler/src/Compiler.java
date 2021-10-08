import wordTokenizer.SequenceTokenizer;
import wordTokenizer.Tokenizer;
import wordTokenizer.Word;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.RandomAccessFile;

public class Compiler {
    public static void main(String[] args) throws Exception {
        // 测试词法分析
        Tokenizer tokenizer = new SequenceTokenizer("testfile.txt");
        Word word;
        BufferedWriter output = new BufferedWriter(new FileWriter("output.txt", false));
        while ((word = tokenizer.next()) != Word.END) {
//            System.out.println(word.getCategory().toString() + " " + word.getName());
//            if (word.getCategory().toString().equals("N")) {
//                break;
//            }
            output.write(word.getCategory().toString() + " " + word.getName() + "\n");
        }
        tokenizer.close();
        output.close();
    }
}

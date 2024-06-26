package org.example.oj.factory.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.oj.entity.exam.Exam;
import org.example.oj.strategy.reader.FileReader;
import org.example.oj.strategy.reader.JsonReader;
import org.example.oj.strategy.reader.XmlReader;

/**
 * @author SummCoder
 * @desc 题目读取的上下文类
 * @date 2024/3/26 22:22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReaderFactory {
    // 读取方法策略类
    private FileReader fileReader;

    public ReaderFactory(String type) {
        // 简单工厂 + 策略模式选择构建Reader策略
        if (type.equals("json")) {
            this.fileReader = new JsonReader();
        } else if (type.equals("xml")) {
            this.fileReader = new XmlReader();
        }
    }

    public Exam getExam(String examPath) {
        return this.fileReader.getExam(examPath);
    }
}

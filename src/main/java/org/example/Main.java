package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.example.oj.constant.Constant;
import org.example.oj.entity.answer.Answer;
import org.example.oj.entity.answer.Answers;
import org.example.oj.entity.exam.Exam;
import org.example.oj.entity.question.ProgrammingQuestion;
import org.example.oj.entity.question.Question;
import org.example.oj.factory.util.ReaderFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Objects;

/**
 * @author SummCoder
 * @date 2024/03/22 23:51
 */

public class Main {
    public static void main(String[] args){
        String casePath = args[0];
        // 题目文件夹路径
        String examsPath = casePath + System.getProperty("file.separator") + "exams";
        // 答案文件夹路径
        String answersPath = casePath + System.getProperty("file.separator") + "answers";
        // 输出文件路径
        String output = args[1];
        Constant.setExamsPath(examsPath);
        Constant.setAnswerPath(answersPath);
        Constant.setOutputPath(output);
        try {
            // TODO:在下面调用你实现的功能
            // 遍历读取所有题目文件夹中的文件
            File examFolder = new File(examsPath);
            File[] files = examFolder.listFiles();
            File answerFolder = new File(answersPath);
            File[] answerFiles = answerFolder.listFiles((dir, name) -> {
                // 只接受文件，不接受文件夹
                return new File(dir, name).isFile();
            });
            assert files != null;
            assert answerFiles != null;
            CSVWriter writer = new CSVWriter(new FileWriter(output), ',', CSVWriter.NO_QUOTE_CHARACTER);
            String[] header;

            if (output.endsWith("output_complexity.csv")) {
                header = new String[]{"examId", "stuId", "qId", "complexity"};
            }else {
                header = new String[]{"examId", "stuId", "score"};
            }
            writer.writeNext(header);
            // 变化的量：文件的类型
            for (File file : files) {
                // 获取文件拓展名，例如json、xml
                String fileName = file.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                // 声明一个文件读取者
                ReaderFactory reader = new ReaderFactory(fileExtension);
                String examPath = file.getAbsolutePath();
                // 获得试卷
                Exam exam = reader.getExam(examPath);
                // 进行评分
                for (File answerFile : answerFiles) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(answerFile);
                    Answers answers = objectMapper.treeToValue(jsonNode, Answers.class);
                    if (output.endsWith("output_complexity.csv")){
                        if (Objects.equals(answers.getExamId(), exam.getId())) {
                            List<Question> questions = exam.getQuestions();
                            List<Answer> answerList = answers.getAnswers();
                            for (Question question : questions) {
                                for (Answer answer : answerList) {
                                    if (question.getType() == 3 && Objects.equals(question.getId(), answer.getId())) {
//                                        if (answers.getExamId() == 4 && answers.getStuId() == 3) {
                                            int complexity;
                                            complexity = ((ProgrammingQuestion)question).calculateCyclomaticComplexity(answer);
                                            String[] record = {String.valueOf(answers.getExamId()), String.valueOf(answers.getStuId()), String.valueOf(question.getId()), String.valueOf(complexity)};
                                            writer.writeNext(record);
//                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if (Objects.equals(answers.getExamId(), exam.getId())) {
                            int score = 0;
                            // 早交或者迟交
                            if (answers.getSubmitTime() < exam.getStartTime() || answers.getSubmitTime() > exam.getEndTime()) { }
                            else {
                                List<Question> questions = exam.getQuestions();
                                List<Answer> answerList = answers.getAnswers();
                                for (Question question : questions) {
                                    for (Answer answer : answerList) {
                                        if (Objects.equals(question.getId(), answer.getId())) {
                                            int singleScore;
                                            singleScore = question.testAnswer(question, answer);
                                            score += singleScore;
                                        }
                                    }
                                }
                            }
                            String[] record = {String.valueOf(answers.getExamId()), String.valueOf(answers.getStuId()), String.valueOf(score)};
                            writer.writeNext(record);
                        }
                    }

                }
            }
            writer.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
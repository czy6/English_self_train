package com.english.langchain4j.config;

import com.english.langchain4j.repository.RedisChatMemoryStore;
// import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
// import dev.langchain4j.data.document.Document;
// import dev.langchain4j.data.document.DocumentSplitter;
// import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
// import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
// import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
// import dev.langchain4j.model.embedding.EmbeddingModel;
// import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
// import dev.langchain4j.rag.content.retriever.ContentRetriever;
// import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
// import dev.langchain4j.store.embedding.EmbeddingStore;
// import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
// import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class CommonConfiguration {

    private final RedisChatMemoryStore redisChatMemoryStore;

    // private final EmbeddingModel embeddingModel;
    // 
    // private final RedisEmbeddingStore redisEmbeddingStore;
    // 
    // private final OpenAiEmbeddingModel openAiEmbeddingModel;

    /**
     * 会话记忆对象
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    /**
     * 创建ChatMemoryProvider对象
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
    }

    /**
     * 构建向量数据库操作
     */
    // @Bean  // -- 不需要每次启动都重新检索content目录创建向量数据库
    // public EmbeddingStore store() {
    //     // 1.加载文件
    //     // 1.1 类路径加载器ClassPathDocumentLoader
    //     List<Document> documents = ClassPathDocumentLoader
    //             .loadDocuments("content", new ApachePdfBoxDocumentParser());
    //     // 预处理文档并分割
    //     // 1.2 文件加载器FileDocumentLoader(磁盘绝对路径)
    //     // 1.3 url加载器UrlDocumentLoader

    //     // 1.4 文档解析器 -- 把非纯文本数据 -> 纯文本
    //     // (
    //     //      纯文本: TextDocumentParser
    //     //      pdf: ApachePdfDocumentParser
    //     //      微软office(DOC,PPT,XLS): ApachePointDocumentParser
    //     //      (默认--在pdf效果不好)解析所有格式: ApacheTikalDocumentParser
    //     // )

    //     // 1.5 文档分割器 -- 把文本数据切割成多个小段
    //     // (
    //     //       按照段落: DocumentByParagraphSplitter
    //     //       按照行: DocumentByLineSplitter
    //     //       按照句子: DocumentBySentenceSplitter
    //     //       按照词: DocumentByWordSplitter
    //     //       按照固定数量字符: DocumentByCharacterSplitter
    //     //       按照正则: DocumentByRegexSplitter
    //     //       递归(单个文本片段默认字符最多300个, 段落-行-句子-词),默认: DocumentSplitter.recursive(...)
    //     // )

    //     // 递归划分
    // //        DocumentSplitter ds = DocumentSplitters.recursive(
    // //                500,  // 单个文本片段最大字符数
    // //                100 // 意思就是让下一片段的检索内容包含上一片段的检索内容长度，
    // //                    // 比如上一片段具有讲述高考，下一片段的检索内容不包含高考但是暗指高考
    // //                    // 就需要将上一片段对应高考内容添加到下一片段的检索内容中，使两者都能检索到
    // //        );


    //     // 段落划分
    //     // DocumentByParagraphSplitter ds = new DocumentByParagraphSplitter(10000, 150);

    //     // 2. 配置段落分割器：适配真题模块化结构，避免过度拆分核心内容
    //     // 配置说明：
    //     // - maxSegmentSize=800：单片段最大800字符（匹配真题阅读文章段落、题干+选项等内容长度，确保单题/单段完整）
    //     // - minSegmentSize=100：单片段最小100字符（防止拆分出"单个选项""半个题干"等无意义小片段，如听力选项、阅读小题干）
    //     DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(800, 100);

    //     // 3. 为真题文档添加专属元数据标签：辅助AI对话时精准识别模块（作文/阅读/翻译/答案）
    //     List<Document> labeledDocuments = documents.stream()
    //             .peek(doc -> {
    //                 // 元数据1：明确文件唯一标识，避免与其他文件混淆
    //                 doc.metadata().put("file_name", "2015年12月英语四级考试真题(第1套).pdf");
    //                 // 元数据2：标注文档类型，便于AI定位"四级真题"场景
    //                 doc.metadata().put("doc_type", "CET-4_Exam_Paper_201512_Set1");
    //                 // 元数据3：标注模块识别关键词（结合真题内容特征，辅助后续检索）
    //                 doc.metadata().put("module_tags", "作文,听力,阅读,翻译,参考答案");
    //             })
    //             .toList();

    //     // 正则划分 第一层分割：按"Part I/II/III/IV"分割（匹配文档中Part I Writing等结构）
    // //        DocumentByRegexSplitter ds = new DocumentByRegexSplitter(
    // //                "Part [IVX]+",  // 匹配罗马数字Part I至Part IV
    // //                " ",
    // //                100000,
    // //                0
    // //        );

    //     // 1.6 向量模型 -- 把切割后的片段向量化或查询时把用户输入的内容向量化 text-embedding-v3

    //     // 2.构建向量数据库操作对象 -- 内存版本的向量数据库
    // //        InMemoryEmbeddingStore store = new InMemoryEmbeddingStore();

    //     // 3.构建一个EmbeddingStoreIngestor对象， 完成文本数据切割，向量化，存储
    //     EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
    // //                .embeddingStore(store)
    //             .embeddingStore(redisEmbeddingStore) // redis版本的向量数据库
    // //                .documentSplitter(ds)
    //             .documentSplitter(paragraphSplitter)
    //             .embeddingModel(embeddingModel)
    //             .build();
    //     ingestor.ingest(documents);

    //     return redisEmbeddingStore;
    // }

    /**
     * 构建向量数据库检索对象
     */
    // @Bean
    // public ContentRetriever contentRetriever(/*EmbeddingStore store  -- 内存版本的向量数据库*/) {
    //     return EmbeddingStoreContentRetriever.builder()
    // //                .embeddingStore(store)
    //             .embeddingStore(redisEmbeddingStore)
    //             .embeddingModel(embeddingModel)
    //             .maxResults(3)
    //             .minScore(0.5)
    //             .build();
    // }

}

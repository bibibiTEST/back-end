package com.example.rc4_backend.tool;

import com.example.rc4_backend.pojo.EmbedderResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataEmbedder {
    private volatile static DataEmbedder dataEmbedder;
    private DataEmbedder(){};
    public int[][] setArray;
    public int matrixCnt = 0;
    public int imageIndex = 0;
    public static DataEmbedder getInstance() {
        if (dataEmbedder == null) {
            synchronized (DataEmbedder.class) {
                if (dataEmbedder == null) {
                    dataEmbedder = new DataEmbedder();
                }
            }
        }
        return dataEmbedder;
    }
    public static final String imageWithInfoPath = "D:\\idea\\project\\rc4_backend\\src\\main\\resources\\image";
    public static final String matrixTxtPath = "D:\\idea\\project\\rc4_backend\\src\\main\\resources\\matrixTxt";
    public EmbedderResponse embedder(MultipartFile file) throws IOException {

        // 加载原始图像
        BufferedImage image = ImageIO.read((File) file);

        // 要嵌入的消息

        String message = new String(Files.readAllBytes(Paths.get("src/main/java/com/xeno/rdhei/util/hidden_message.txt")));

        // 将消息转换为二进制字符串
        String binaryMessage = stringToBinary(message) + "00000000"; // 添加结束标志
        System.out.println(binaryMessage);
        // 块大小
        int blockSize = 8;

        // data-hiding key
        String dataHidingKey = "an dataHidingKey";
        // 嵌入信息
        BufferedImage embeddedImage = embedMessage(image, binaryMessage, blockSize, dataHidingKey);
        String filePath = matrixTxtPath + matrixCnt + ".png";
        writeArrayToFile(setArray, filePath);
        matrixCnt++;
        // 保存嵌入后的图像
        ImageIO.write(embeddedImage, "png", new File(imageWithInfoPath + imageIndex));
        EmbedderResponse response = new EmbedderResponse();
        response.setImageIndex(imageIndex);
        response.setSetArray(setArray);
        return response;
    }

    // 将字符串转换为二进制
    private String stringToBinary(String message) {
        StringBuilder binary = new StringBuilder();
        for (char c : message.toCharArray()) {
            binary.append(String.format("%08d", Integer.parseInt(Integer.toBinaryString(c))));
        }
        return binary.toString();
    }

    // 根据 data-hiding key 随机划分像素到集合 S0 和 S1
    private List<Integer>[] dividePixels(int blockSize, Random random) {
        List<Integer>[] sets = new List[2];
        sets[0] = new ArrayList<>();
        sets[1] = new ArrayList<>();
        for (int i = 0; i < blockSize * blockSize; i++) {
            int setIndex = random.nextBoolean() ? 0 : 1;
            sets[setIndex].add(i);
        }
        return sets;
    }

    // 嵌入信息到图像
    private BufferedImage embedMessage(BufferedImage image, String binaryMessage, int blockSize, String key) {
        int width = image.getWidth();
        int height = image.getHeight();
        int bitIndex = 0;

        setArray  = new int[width][height];
        // 使用 data-hiding key 初始化随机数生成器
        Random random = new Random(key.hashCode());

        for (int blockY = 0; blockY < height / blockSize; blockY++) {
            for (int blockX = 0; blockX < width / blockSize; blockX++) {
                List<Integer>[] sets = dividePixels(blockSize, random);
                int startX = blockX * blockSize;
                int startY = blockY * blockSize;
                for(int c=0;c<2;c++)
                {
                    for(int index : sets[c])
                    {
                        int x = startX + index % blockSize;
                        int y = startY + index / blockSize;
                        setArray[x][y] = c;
                    }
                }
                if (bitIndex >= binaryMessage.length()) {
                    System.out.printf("现在退出……，当前bitIndex:%d\n",bitIndex);
                    System.out.printf("blockX:%d,blockY:%d\n",blockX,blockY);
                    continue;
                }
                int bitToEmbed = binaryMessage.charAt(bitIndex) - '0';
                List<Integer> targetSet = sets[bitToEmbed];
                for (int index : targetSet) {
                    int x = startX + index % blockSize;
                    int y = startY + index / blockSize;
                    int pixel = image.getRGB(x, y);
                    int modifiedPixel = pixel ^ 0x07;

                    image.setRGB(x, y, modifiedPixel);
                }
                bitIndex++;
            }
        }
        return image;
    }
    public void writeArrayToFile(int[][] array, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int[] row : array) {
                StringBuilder line = new StringBuilder();
                for (int value : row) {
//                    System.out.print(value);
//                    System.out.print(" ");
                    line.append(value).append(" ");
                }
//                System.out.println();
                writer.write(line.toString().trim());
                writer.newLine();
            }
        }
    }


}

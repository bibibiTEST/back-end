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
    public DataEmbedder(int blockSize,int groupSize,String dataHidingKey){
        this.blockSize = blockSize;
        this.groupSize = groupSize;
        this.dataHidingKey = dataHidingKey;
    };
    public DataEmbedder()
    {
        this(8,8,"an dataHidingKey");
    }
//    public int[][] setArray;
//    public int matrixCnt = 0;
    public int imageIndex;
    // 块大小
    private int blockSize;
    //将图像分为n个块， n = width * height / (blockSize * blockSize)
    //将k个块划分为一组，共有g =  n/groupSize 组。分组方式由dataHidingKey随机生成
    //此处根据dataHidingKey生成n个不重复的随机数，范围0-n，存入blockOrder,表示打乱后的block顺序。
    //在blockOrder中，每groupSize个块表示一个组。
    private int groupSize;
    private int[] blockOrder;
    private int orderCnt = 0;
    private int matrixCnt = 0;
    // data-hiding key
    private String dataHidingKey;


    public static final String imageWithInfoPath = "D:\\idea\\project\\rc4_backend\\src\\main\\resources\\image";
    public static final String matrixTxtPath = "D:\\idea\\project\\rc4_backend\\src\\main\\resources\\matrixTxt";
    public static final String orderTxtPath = "D:\\idea\\project\\rc4_backend\\src\\main\\resources\\orderTxt";
    public EmbedderResponse Embed(MultipartFile file) throws IOException {

        // 加载原始图像
        BufferedImage image = ImageIO.read((File) file);

        // 要嵌入的消息

        String message = new String(Files.readAllBytes(Paths.get("src/main/java/com/xeno/rdhei/util/hidden_message.txt")));

        // 将消息转换为二进制字符串
        String binaryMessage = stringToBinary(message) + "00000000"; // 添加结束标志
        System.out.println(binaryMessage);

        // 嵌入信息
        BufferedImage embeddedImage = embedMessage(image, binaryMessage, dataHidingKey);

        //旧--写入s矩阵
//        String filePath = matrixTxtPath + matrixCnt + ".png";
//        writeArrayToFile(setArray, filePath);
//        matrixCnt++;
        //写入块order
        String filePath = orderTxtPath + orderCnt + ".png";
        saveBlockOrder(blockOrder,filePath);
        orderCnt++;

        // 保存嵌入后的图像
        ImageIO.write(embeddedImage, "png", new File(imageWithInfoPath + imageIndex));
        EmbedderResponse response = new EmbedderResponse();
        response.setImageIndex(imageIndex);
        response.setBlockOrder(blockOrder);
//        response.setSetArray(setArray);
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

    // 根据 data-hiding key 打乱块的顺序
    private void divideBlocks(int width,int height, Random random) {
        int n = width/blockSize * height/blockSize;
        blockOrder = new int[n];
        for(int i=0;i<n;i++)
        {
            blockOrder[i] = i;
        }
        for(int i=n-1;i>0;i--)
        {
            int j = random.nextInt(i+1);
            int temp = blockOrder[i];
            blockOrder[i] = blockOrder[j];
            blockOrder[j] = temp;
        }
    }
    private BufferedImage getBlockByOrder(BufferedImage image,int order)
    {
        int width = image.getWidth();
        int blocksPerRow = width/blockSize;

        int blockRow = order / blocksPerRow;
        int blockCol = order % blocksPerRow;

        // 块的左上角坐标
        int startX = blockCol * blockSize;
        int startY = blockRow * blockSize;

        // 裁剪出块 subImage子图像，引用原图像的某一块……
        return image.getSubimage(startX, startY, blockSize, blockSize);
    }
    // 嵌入信息到图像
    private BufferedImage embedMessage(BufferedImage image, String binaryMessage, String dataHidingKey) {
        int width = image.getWidth();
        int height = image.getHeight();
        Random random = new Random(dataHidingKey.hashCode());
        divideBlocks(width,height,random);
        int bitIndex = 0;

        int t = (int) (Math.log(groupSize) / Math.log(2)); // t = log2(groupSize)
        int n = width * height / (blockSize * blockSize);
        int g = n/groupSize;

        BufferedImage embeddedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        embeddedImage.getGraphics().drawImage(image, 0, 0, null);

        while (bitIndex + t <= g) {
            String binaryNumber = binaryMessage.substring(bitIndex, bitIndex + t);
            int decimalNumber = Integer.parseInt(binaryNumber, 2); // 转换为十进制数

            // 获取组中第 m+1 个块
            int startIndexInGroup = (bitIndex / t) * groupSize; // 组的起始索引
            int blockIndex = startIndexInGroup + decimalNumber;
            if (blockIndex >= blockOrder.length) {
                break; // 防止越界
            }
            int blockOrderIndex = blockOrder[blockIndex];
            BufferedImage block = getBlockByOrder(embeddedImage, blockOrderIndex);
            modifyBlock(block);
            bitIndex += t;
        }
        return embeddedImage;
    }
    private void modifyBlock(BufferedImage block) {
        int width = block.getWidth();
        int height = block.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((x + y) % 2 == 0) {
                    int rgb = block.getRGB(x, y);
                    int modifiedPixel = rgb ^ 0x07;
                    block.setRGB(x, y, modifiedPixel);
                }
            }
        }
    }
    //写BlockOrder入文件。
    private void saveBlockOrder(int[] blockOrder, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int order : blockOrder) {
                writer.write(order + " ");
            }
        }
    }
    //写s矩阵入文件。
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

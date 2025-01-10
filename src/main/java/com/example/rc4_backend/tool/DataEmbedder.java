package com.example.rc4_backend.tool;

import com.example.rc4_backend.pojo.EmbedderResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DataEmbedder {
    private static class DataEmbedderHolder {
        private static final DataEmbedder INSTANCE = new DataEmbedder();
    }
    public static DataEmbedder getInstance() {
        return DataEmbedderHolder.INSTANCE;
    }

    // 块大小
    private static final int blockSize = 8;
    //将图像分为n个块， n = width * height / (blockSize * blockSize)
    //将k个块划分为一组，共有g =  n/groupSize 组。分组方式由dataHidingKey随机生成
    //此处根据dataHidingKey生成n个不重复的随机数，范围0-n，存入blockOrder,表示打乱后的block顺序。
    //在blockOrder中，每groupSize个块表示一个组。
    private static final int groupSize = 8;
    private List<Integer> blockOrder;
    public static final String imageWithInfoPath = "src\\main\\resources\\image";
    private static final String dataHidingKey = "an dataHidingKey";
    public static final String orderTxtPath = "src\\main\\resources\\orderTxt";
    public EmbedderResponse Embed(MultipartFile file) throws IOException {
        // 加载原始图像
        BufferedImage image = ImageIO.read(file.getInputStream());
        // 要嵌入的消息

//        String message = new String(Files.readAllBytes(Paths.get("")));
        String message = "letsGo!";
        // 将消息转换为二进制字符串
        String binaryMessage = stringToBinary(message) + "000000000"; // 添加结束标志
        System.out.println(binaryMessage);
        // 嵌入信息
        BufferedImage embeddedImage = embedMessage(image, binaryMessage, dataHidingKey);
        //旧--写入s矩阵
//        String filePath = matrixTxtPath + matrixCnt + ".png";
//        writeArrayToFile(setArray, filePath);
//        matrixCnt++;
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        // 格式化时间戳为文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        String formattedTimestamp = sdf.format(new Date(timestamp));
        // 写入块order
        String orderTxtName = formattedTimestamp + ".txt";
        File orderTxt = new File(orderTxtPath, orderTxtName);
        saveBlockOrder(blockOrder, orderTxt);
        // 保存嵌入后的图像
        String embedderdFileName = formattedTimestamp + ".png"; // 使用 PNG 格式，可以根据需要更改
        File embedderdFile = new File(imageWithInfoPath, embedderdFileName);
        ImageIO.write(embeddedImage, "png", embedderdFile);
        EmbedderResponse response = new EmbedderResponse();
        // 嵌入后图片的路径
        response.setEmbedderdFilePath(embedderdFile.getAbsolutePath());
        response.setBlockOrder(blockOrder);
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
        int block_per_row_down = width/blockSize;
        int block_per_column_down = height/blockSize;
        int n = block_per_column_down * block_per_row_down;

        int block_per_row_up = (int) Math.ceil(1.0 * width / blockSize);
        int block_per_column_up = (int) Math.ceil(1.0 * height / blockSize);

        int totalBlock = block_per_row_up*block_per_column_up;

        boolean row_cut = block_per_column_up != block_per_column_down;
        boolean column_cut = block_per_row_up != block_per_row_down;

        blockOrder = new ArrayList<>(totalBlock);
        for(int i=0;i<totalBlock;i++)
        {
            blockOrder.add(i);
        }
        System.out.println(blockOrder.size());
        if(row_cut)
        {
            for(int i=0;i<block_per_row_up;i++)
            {
                int order_be_cut = block_per_row_up * (block_per_column_up-1) + i;
                System.out.printf("剔除：%d\n",order_be_cut);
                blockOrder.remove((Integer) order_be_cut);
            }
        }
        if(column_cut)
        {
            for(int i=0;i<block_per_column_up;i++)
            {
                int order_be_cut = (block_per_row_up-1) + i*(block_per_row_up);
                System.out.printf("剔除：%d\n",order_be_cut);
                blockOrder.remove((Integer)order_be_cut);
            }
        }
        System.out.println(blockOrder.size());
        for(int i=n-1;i>0;i--)
        {
            int j = random.nextInt(i+1);
            int temp = blockOrder.get(i);
            blockOrder.set(i, blockOrder.get(j));
            blockOrder.set(j, temp);
        }
    }
    private BufferedImage getBlockByOrder(BufferedImage image,int order)
    {
        int width = image.getWidth();
        int blocksPerRow = (int) Math.ceil(1.0 * width / blockSize);

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
        int n = (width/blockSize) * (height/blockSize);
        int g = n/groupSize;
        System.out.println(image.getType());
        BufferedImage embeddedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        embeddedImage.getGraphics().drawImage(image, 0, 0, null);
        System.out.printf("length: %d\n",binaryMessage.length());
        System.out.printf("groupNum:%d\n",g);
        while (bitIndex < binaryMessage.length()){
            int next = Math.min(bitIndex + t, binaryMessage.length());
            String binaryNumber = binaryMessage.substring(bitIndex, next);
            System.out.printf(binaryNumber+" ");
            int decimalNumber = Integer.parseInt(binaryNumber, 2); // 转换为十进制数
            System.out.printf("%d ",decimalNumber);
            System.out.println();
            // 获取组中第 m+1 个块
            int startIndexInGroup = (bitIndex / t) * groupSize; // 组的起始索引
            int blockIndex = startIndexInGroup + decimalNumber;
            if (blockIndex >= blockOrder.size()) {
                break; // 防止越界
            }
            int blockOrderIndex = blockOrder.get(blockIndex);
            BufferedImage block = getBlockByOrder(embeddedImage, blockOrderIndex);
            modifyBlock(block);
            bitIndex += t;
        }
        return embeddedImage;
    }
    public void test(String path,int x,int y) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        int rgb = image.getRGB(x,y);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int gray = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
        int grayValue = image.getRaster().getSample(x, y, 0);

        int _green = green ^ 0b00001000;
        int modifiedRGB = (rgb & 0xFFFF00FF) | (green << 8);

        System.out.printf("%d,%d坐标值：blue:%d,green:%d,red:%d,gray:%d _green:%d, type: %d\n",x,y,blue,green,red,gray,_green,image.getType());
    }
    private void modifyBlock(BufferedImage block) {
        for (int y = 0; y < blockSize; y++) {
            for (int x = 0; x < blockSize    ; x++) {
                if ((x + y) % 2 == 0) {
                    int rgb = block.getRGB(x,y);
                    int red = (rgb>>16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    green = green ^ 0b00001000;
                    Color color = new Color(red,green,blue);
                    block.setRGB(x, y, color.getRGB());
                }
            }
        }
    }

    //写BlockOrder入文件。
    private void saveBlockOrder(List<Integer> blockOrder, File orderTxt) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(orderTxt))) {
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

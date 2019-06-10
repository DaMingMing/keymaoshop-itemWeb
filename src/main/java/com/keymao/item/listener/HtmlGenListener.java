package com.keymao.item.listener;

import com.keymao.item.pojo.Item;
import com.keymao.pojo.TbItem;
import com.keymao.pojo.TbItemDesc;
import com.keymao.service.ItemService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * 监听商品添加信息，生成对应的静态页面
 */
public class HtmlGenListener implements MessageListener {

    @Autowired
    private ItemService itemService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Value("${HTML_GEN_PATH}")
    private String HTML_GEN_PATH;

    @Override
    public void onMessage(Message message) {
        try {
            //创建模板，参考jsp

            //从消息中取商品ID
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            Long itemId = new Long(text);
            //根据商品id获取商品信息，商品描述
            //等待事务提交，此处休眠是因为添加商品之后事务可能还没有提交，这时候如果通过ID获取商品就获取不到。
            //也可以直接在商品添加的控制层发送到MQ，就无需休眠
            Thread.sleep(1000);
            TbItem tbItem = itemService.getItemById(itemId);
            //把TbItem转换成Item对象
            Item item = new Item(tbItem);
            //根据商品id查询商品描述
            TbItemDesc itemDesc = itemService.getItemDescById(itemId);
            //创建一个数据集，把商品数据封装
            Map map = new HashMap<>();
            map.put("item",item);
            map.put("itemDesc",itemDesc);
            //加载模板对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            //创建一个输出流，指定输出的目录及文件名
            Writer out = new FileWriter(new File(HTML_GEN_PATH + itemId + ".html"));
            //生成静态页面
            template.process(map, out);
            //关闭流
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}

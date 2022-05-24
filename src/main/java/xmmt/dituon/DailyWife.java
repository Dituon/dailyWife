package xmmt.dituon;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DailyWife extends JavaPlugin {
    public static final DailyWife INSTANCE = new DailyWife();
    public static HashMap<Group, Member> haveWifeGroup = new HashMap<>();
    public static Bot bot = null;

    private DailyWife() {
        super(new JvmPluginDescriptionBuilder("xmmt.dituon.dailyWife", "1.0")
                .name("DailyWife")
                .author("Dituon")
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded!");
        GlobalEventChannel.INSTANCE.subscribeOnce(BotOnlineEvent.class, e -> {
            if (bot == null) {
                bot = e.getBot();
                bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, this::onGroupMessage);
                showTimer();
            }
        });
    }


    private void onGroupMessage(GroupMessageEvent e) {
        if (e.getMessage().contentToString().equals("wife")) {
            try {
                if (isHaveWife(e.getGroup())) {
                    Image image = overlyingImage(haveWifeGroup.get(e.getGroup()));
                    assert image != null;
                    String name = haveWifeGroup.get(e.getGroup()).getNameCard().equals("") ?
                            haveWifeGroup.get(e.getGroup()).getNick():
                            haveWifeGroup.get(e.getGroup()).getNameCard();
                    e.getGroup().sendMessage(new QuoteReply(e.getMessage())
                            .plus(name)
                            .plus(new PlainText(" 是今天的迫害对象!\n"))
                            .plus(image)
                    );
                } else {
                    haveWifeGroup.put(e.getGroup(), e.getSender());
                    Image image = overlyingImage(e.getSender());
                    assert image != null;
                    e.getGroup().sendMessage(new PlainText("还没有迫害对象! 就是你了!!!\n")
                            .plus(image)
                            .plus(new At(e.getSender().getId())));
                }
            } catch (IOException ex) {
                e.getGroup().sendMessage(new QuoteReply(e.getMessage()).plus("可恶..出错了..."));
                getLogger().error(ex);
            }
            return;
        }
        if (!isHaveWife(e.getGroup())) {
            Random random = new Random();
            int r = random.nextInt(10);
            if (r == 0) {
                haveWifeGroup.put(e.getGroup(), e.getSender());
                if(e.getGroup().getId()==826801772L){
                    return;
                }
                try {
                    Image image = overlyingImage(e.getSender());
                    assert image != null;
                    e.getGroup().sendMessage(new PlainText("今天群友的迫害对象是...\n")
                            .plus(image)
                            .plus(new At(e.getSender().getId()))
                            .plus(new PlainText("酱!")));
                } catch (IOException ex) {
                    getLogger().error(ex);
                }
            }
        }
    }

    public void showTimer() {
        long dayS = 24 * 60 * 60 * 1000;
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '00:00:00'");
        Date startTime = null;
        try {
            startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert startTime != null;
        if (System.currentTimeMillis() > startTime.getTime()) {
            startTime = new Date(startTime.getTime() + dayS);
        }

        java.util.Timer t = new java.util.Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                haveWifeGroup.clear();
            }
        };
        t.scheduleAtFixedRate(task, startTime, dayS);
    }

    BufferedImage randomTexture() throws IOException {
        File dir = new File("./res/wife/");
        getLogger().info(dir.getAbsolutePath());
        String[] children = dir.list();
        Random random = new Random();

        if (children == null) {
            getLogger().error("请检查 Mirai/res/wife/ 文件夹");
            return null;
        }
        int i = random.nextInt(children.length - 1);
        getLogger().info(children[i]);

        File f = new File("./res/wife/" + children[i]);
        return ImageIO.read(f);
    }

    public static BufferedImage getBufferedImage(String URL) {
        HttpURLConnection conn = null;
        BufferedImage image = null;
        try {
            URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            image = ImageIO.read(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert conn != null;
            conn.disconnect();
        }
        return image;
    }

    Image overlyingImage(Member m) throws IOException {
        BufferedImage buffImg = getBufferedImage(m.getAvatarUrl());
        BufferedImage waterImg = Objects.requireNonNull(randomTexture());
        Graphics2D g2d = buffImg.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0F));
        g2d.drawImage(waterImg, 0, 0, 640, 640, null);
        g2d.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(buffImg, "png", os);
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            ExternalResource res = ExternalResource.create(input);
            Image img = m.uploadImage(res);
            res.close();
            return img;
        } catch (IOException e) {
            getLogger().error(e);
            return null;
        }
    }

    private boolean isHaveWife(Group group) {
        if (haveWifeGroup != null && !haveWifeGroup.isEmpty()) {
            return haveWifeGroup.containsKey(group);
        } else {
            return false;
        }
    }
}
package com.wzy.api.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wzy.api.config.ApiPathConfig;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import common.Exception.BusinessException;
import common.Utils.AuthPhoneNumber;
import common.model.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
public class InterfaceController {

    @Resource
    private YuCongMingClient client;

    @Resource
    private ApiPathConfig apiPathConfig;

    @Value("${img.url}")
    private String base_url;

    @Value("${api_interface.ai_chat}")
    private String AiModelId;

    @Value("${api_interface.emoji}")
    private String EmojiModelId;

    @Value("${api_interface.ppt_outline}")
    private String PPTModelId;

    @Value("${api_interface.rating_generation}")
    private String RatingModelId;

    @GetMapping("/ai/chat")
    public String chatWithAI(Object parmeter) throws Exception {
        byte[] bytes = parmeter.toString().getBytes("iso8859-1");
        parmeter = new String(bytes,"utf-8");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(Long.valueOf(AiModelId));
        devChatRequest.setMessage(parmeter.toString());

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new RuntimeException();
        }
        return response.getData().getContent();
    }

    @GetMapping("/emoji/change")
    public String emojiChange(Object parmeter) throws Exception {
        byte[] bytes = parmeter.toString().getBytes("iso8859-1");
        parmeter = new String(bytes,"utf-8");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(Long.valueOf(EmojiModelId));
        devChatRequest.setMessage(parmeter.toString());

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new RuntimeException();
        }
        return response.getData().getContent();
    }

    @GetMapping("/img/getByRandom")
    public String getImageByRandom(Object parmeter) {
        Random random = new Random();
        // 随机生成1到200数字
        int num = random.nextInt(200) + 1;
        String url = base_url + num + ".png";
        return url;
    }

    @GetMapping("/parmeter/get")
    public String getparmeterByGet(Object parmeter) throws Exception {
        byte[] bytes = parmeter.toString().getBytes("iso8859-1");
        parmeter = new String(bytes,"utf-8");
        return "GET 你的名字是：" + parmeter;
    }

    @GetMapping("/ppt/generation")
    public String generatePPT(Object parmeter) throws Exception {
        byte[] bytes = parmeter.toString().getBytes("iso8859-1");
        parmeter = new String(bytes,"utf-8");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(Long.valueOf(PPTModelId));
        devChatRequest.setMessage(parmeter.toString());

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new RuntimeException();
        }
        return response.getData().getContent();
    }

    @GetMapping("/rating/generation")
    public String generateRating(Object parmeter) throws Exception {
        byte[] bytes = parmeter.toString().getBytes("iso8859-1");
        parmeter = new String(bytes,"utf-8");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(Long.valueOf(RatingModelId));
        devChatRequest.setMessage(parmeter.toString());

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new RuntimeException();
        }
        return response.getData().getContent();
    }

    @GetMapping("/tel/getNumberHome")
    public String getNumberHome(Object tel) {
        HttpResponse response = null;
        String num = tel.toString();
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        boolean isValid = authPhoneNumber.isPhoneNum(num);
        if (!isValid) {
            throw new BusinessException(ErrorCode.API_INVOKE_ERROR, "手机号非法");
        }
        // 访问第三方接口
        response = HttpRequest.post(apiPathConfig.getTeladdress() + num)
                .timeout(30000)//超时，毫秒
                .execute();
        String address = null;
        if (response.getStatus() != 200) {
            throw new BusinessException(ErrorCode.API_INVOKE_ERROR, "第三方接口调用错误");
        }
        String body = response.body();
        String data = getData(body);
        JSONObject jsonObject1 = JSONUtil.parseObj(data);
        address = jsonObject1.getStr("address");
        return address;
    }

    private String getData(String body) {
        JSONObject jsonObject = JSONUtil.parseObj(body);
        String code = jsonObject.getStr("code");
        if (!code.equals("200")) {
            throw new BusinessException(ErrorCode.API_INVOKE_ERROR, "第三方接口调用错误");
        }
        String data = jsonObject.getStr("data");
        return data;
    }

    @GetMapping("/word/getByRandom")
    public String getWordByRandom(Object parmeter) {
        Random random = new Random();
        int num = random.nextInt(86);
        String word = interestingSentences[num];
        return word;
    }

    @GetMapping("/get/qqimg")
    public String getQQImg(Object parmeter) {
        String url = apiPathConfig.getQqimg() + parmeter;
        HttpResponse response = HttpRequest.get(url).timeout(30000).execute();
        Map<String, List<String>> headers = response.headers();
        String s = headers.get("location").get(0);
        return s;
    }

    @PostMapping("/baidu/hot")
    public String getBaiduHot(Object parmeter) {
        String url = apiPathConfig.getBaiduhot();
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        JSONArray jsonArray = JSONUtil.parseArray(json);
        List<String> parmeterList = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String x = jsonObject.getStr("name");
            parmeterList.add(x);
        }
        String s = JSONUtil.toJsonStr(parmeterList);
        return s.substring(1, s.length() - 1).replace("\"", "");
    }

    @PostMapping("/douyin/hot")
    public String getDouYinHot(Object parmeter) {
        String url = apiPathConfig.getDouyinhot();
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        JSONArray jsonArray = JSONUtil.parseArray(json);
        List<String> parmeterList = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String x = jsonObject.getStr("name");
            parmeterList.add(x);
        }
        String s = JSONUtil.toJsonStr(parmeterList);
        return s.substring(1, s.length() - 1).replace("\"", "");
    }

    @PostMapping("/weibo/hot")
    public String getWeiboHot(Object parmeter) {
        String url = apiPathConfig.getWeibohot();
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        JSONArray jsonArray = JSONUtil.parseArray(json);
        List<String> parmeterList = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String x = jsonObject.getStr("name");
            parmeterList.add(x);
        }
        String s = JSONUtil.toJsonStr(parmeterList);
        return s.substring(1, s.length() - 1).replace("\"", "");
    }

    @PostMapping("/zhihu/hot")
    public String getZhiHuHot(Object parmeter) {
        String url = apiPathConfig.getZhihuhot();
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        JSONArray jsonArray = JSONUtil.parseArray(json);
        List<String> parmeterList = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String x = jsonObject.getStr("name");
            parmeterList.add(x);
        }
        String s = JSONUtil.toJsonStr(parmeterList);
        return s.substring(1, s.length() - 1).replace("\"", "");
    }

    @PostMapping("/random/color")
    public String getRandomColor(Object parmeter) {
        String url = apiPathConfig.getRandomcolor();
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        JSONObject obj = JSONUtil.parseObj(json);
        String s = obj.getStr("color");
        return s;
    }

    @PostMapping("/customer/ip")
    public String getCustomerIP(Object parmeter) {
        String url = apiPathConfig.getIpinfo() + parmeter;
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        return json.substring(1, json.length() - 1).replace("\"", "");
    }

    @PostMapping("/rubbish/tel")
    public String getIfRubbishTel(Object parmeter) {
        String url = apiPathConfig.getTelvalid() + parmeter;
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        return json.substring(1, json.length() - 1).replace("\"", "");
    }

    @PostMapping("/history/today")
    public String getHistoryToday(Object parmeter) {
        String url = apiPathConfig.getHistorytoday();
        HttpResponse response = HttpRequest.post(url).timeout(30000).execute();
        String body = response.body();
        String json = getData(body);
        JSONObject data = JSONUtil.parseObj(json);
        String list = data.getStr("list");
        JSONArray jsonArray = JSONUtil.parseArray(list);
        List<String> parmeterList = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String x = jsonObject.getStr("title");
            parmeterList.add(x);
        }
        String s = JSONUtil.toJsonStr(parmeterList);
        return s.substring(1, s.length() - 1).replace("\"", "");
    }


    private String[] interestingSentences = new String[] {
            "梦想是指引我们前进的灯塔。",
            "每一次微笑都是世界上的一个美好瞬间。",
            "编程就像是魔法，轻轻一敲，创造出惊人的东西。",
            "每一天都是一个新的机会。",
            "人生就像一本书，每一天都在写新的章节。",
            "成功的秘诀就是坚持不懈。",
            "阅读是打开新世界的钥匙。",
            "路是脚下走出来的，历史是人们写出来的。",
            "不是所有的航海都会遇到顺风。",
            "唯有热爱，可以抵挡岁月的荒凉。",
            "有时候，改变视角就是一个全新的世界。",
            "每一滴雨水都是大海的一部分。",
            "星星在黑夜中闪烁，就像梦想在心中生长。",
            "你的价值不是别人眼中的你，而是你心中的你。",
            "有些路只有走过，才知道有多精彩。",
            "人生就是一场旅行，我们都是在路上的旅人。",
            "你的笑容就是我的阳光。",
            "时间是最好的医生，也是最好的推手。",
            "有些梦想，即使遥不可及，也要努力追求。",
            "每一滴汗水都是成功的预告。",
            "无论在哪里，都要热爱生活。",
            "世界上最美的风景，是内心的宁静。",
            "让我们把事情做得更好，而不是找借口。",
            "勇敢的人和智慧的人，一样都不能少。",
            "每一次尝试，都是一次可能的成功。",
            "人生不是走过的路，而是感受过的瞬间。",
            "世界这么大，我想去看看。",
            "你要相信，好事总会发生的。",
            "阅读，因为这个世界太精彩。",
            "只要心中有梦想，世界就会为你让路。",
            "把握现在，就是最好的准备。",
            "你的故事，写得精彩些。",
            "要爱，要笑，要珍惜。",
            "爱生活，生活也会爱你。",
            "每一个梦想，都值得我们去追逐。",
            "当你熟悉了黑暗，你就能找到光明。",
            "不要等待机会，而要创造机会。",
            "希望是最好的驱动力。",
            "每一天都是最好的一天。",
            "人生就是一场冒险，勇往直前。",
            "生活，就是要有爱和热情。",
            "想象力是创新的源泉。",
            "有时候，最小的事情却能带来最大的快乐。",
            "不要害怕失败，因为失败是成功的教科书。",
            "你的心情，决定你看到的世界。",
            "知识是力量，但热情才能点燃它。",
            "每一天都是一个新的开始。",
            "每一个心跳，都是生命的节奏。",
            "把握每一刻，生活就是现在。",
            "成功，就是从失败走向失败，而热情不减。",
            "勇敢去追求你的梦想，因为你值得。",
            "人生就是一场马拉松，不在乎起点，只在乎终点。",
            "任何时候，都不要忘记微笑。",
            "你的梦想，就是你的方向。",
            "一切的美好，都始于相信。",
            "你的世界，由你创造。",
            "勇敢追梦，不畏前行。",
            "世界在你的心中，你在世界中。",
            "真正的胜利，是为了梦想而战。",
            "你的每一次努力，都让世界变得更美好。",
            "每一次挑战，都是一次成长的机会。",
            "你的每一次呼吸，都是生命的证明。",
            "生活就是一场旅行，享受每一次的风景。",
            "每一次的失败，都是成功的垫脚石。",
            "梦想就像星星，虽远但明亮。",
            "勇敢追求你的梦想，因为你值得。",
            "人生就是一场冒险，勇往直前。",
            "生活中的每一次选择，都是一次生命的独白。",
            "唯有热爱，可以抵挡岁月的荒凉。",
            "成功就是，跌倒七次，站起八次。",
            "人生就像一场马拉松，不在乎起点，只在乎终点。",
            "你的世界，由你创造。",
            "你的每一次努力，都让世界变得更美好。",
            "每一次的失败，都是成功的垫脚石。",
            "勇敢追求你的梦想，因为你值得。",
            "人生就是一场冒险，勇往直前。",
            "梦想就像星星，虽远但明亮。",
            "生活中的每一次选择，都是一次生命的独白。",
            "你的世界，由你创造。",
            "你的每一次努力，都让世界变得更美好。",
            "每一次的失败，都是成功的垫脚石。",
            "唯有热爱，可以抵挡岁月的荒凉。",
            "成功就是，跌倒七次，站起八次。",
            "人生就像一场马拉松，不在乎起点，只在乎终点。",
            "你的世界，由你创造。",
            "你的每一次努力，都让世界变得更美好。"};

}

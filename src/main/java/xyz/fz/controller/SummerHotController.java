package xyz.fz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.fz.domain.SummerHot;
import xyz.fz.service.SummerHotService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fz on 2016/8/8.
 */
@Controller
@RequestMapping("/summerHot")
public class SummerHotController {

    private static final Logger logger = LoggerFactory.getLogger(SummerHotController.class);

    @Value("${summer.hot.value}")
    private String value;

    @Value("${summer.hot.random-uuid}")
    private String randomUuid;

    @Value("${summer.hot.random-int}")
    private int randomInt;

    @Value("${summer.hot.servers}")
    private String servers;

    @Autowired
    private SummerHotService summerHotService;

    @RequestMapping("/log")
    @ResponseBody
    public Map<String, Object> log() {

        logger.debug("debug调试");
        logger.info("info信息");
        logger.warn("warn警告");
        logger.error("error错误");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "ok logging");
        return result;
    }

    @RequestMapping("/value")
    @ResponseBody
    public String value() {

        logger.debug(value);
        logger.debug(randomUuid);
        logger.debug(randomInt + "");
        logger.debug(servers);
        return "ok value";
    }

    @RequestMapping("/template/{templateName}")
    public String template(Model model,
                           @PathVariable String templateName) {

        model.addAttribute("templateName", templateName);
        return "summerHot/template";
    }

    @RequestMapping("/jdbcTemplate")
    @ResponseBody
    public List<Map<String, Object>> jdbcTemplate() {

        return summerHotService.summerHot();
    }

    @RequestMapping("/jpaPage")
    @ResponseBody
    public Map<String, Object> jpaPage(@RequestParam(value = "draw", required = false, defaultValue = "0") int draw,
                                       @RequestParam(value = "start", required = false, defaultValue = "0") int start,
                                       @RequestParam(value = "length", required = false, defaultValue = "20") int length,
                                       @RequestParam("name") String name) {

        int curPage = (start / length);
        Map<String, Object> result = new HashMap<>();
        Page<SummerHot> page = summerHotService.pageSummerHot(name, curPage, length);
        result.put("draw", draw);
        result.put("recordsTotal", page.getTotalElements());
        result.put("recordsFiltered", page.getTotalElements());
        result.put("data", page.getContent());
        return result;
    }

    @RequestMapping("/jpaSave/{name}")
    @ResponseBody
    public SummerHot jpaSave(@PathVariable String name) {
        return summerHotService.saveSummerHot(name);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setRandomUuid(String randomUuid) {
        this.randomUuid = randomUuid;
    }

    public void setRandomInt(int randomInt) {
        this.randomInt = randomInt;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }
}

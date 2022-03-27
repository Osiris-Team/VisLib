package com.osiris.vislib;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

class GithubTest {

    @Test
    void test() {
        System.out.println(DateFormatUtils.format(new Date(System.currentTimeMillis() - 86400000), "yyyy-MM-dd"));
        //DateTimeFormatter.ofPattern().format(.toInstant());
    }
}
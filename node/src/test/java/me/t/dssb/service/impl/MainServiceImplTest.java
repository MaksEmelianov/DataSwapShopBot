package me.t.dssb.service.impl;

import me.t.dssb.dao.RawDataDAO;
import me.t.dssb.entity.RawData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MainServiceImplTest {
    @Autowired
    private RawDataDAO rawDataDAO;

    @Test
    void testSaveRawData() {
        Update update = new Update();
        Message msg = new Message();
        msg.setText("hello");

        RawData rawData = RawData.builder().event(update).build();
        Set<RawData> testData = new HashSet<>();

        testData.add(rawData);
        rawDataDAO.save(rawData);

        assertThat(testData.contains(rawData)).isTrue();
//        Assert.isTrue(testData.contains(rawData), "Entity not found in the set");
    }
}

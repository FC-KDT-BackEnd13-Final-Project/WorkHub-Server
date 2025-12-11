package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.CheckListCommentResponse;
import com.workhub.checklist.service.CheckListCommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCheckListCommentService {

    private final CheckListCommentService checkListCommentService;

    public CheckListCommentResponse create() {

        return null;
    }

}

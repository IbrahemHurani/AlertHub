package com.alerthub.action.service;

import com.alerthub.action.entity.Action;
import com.alerthub.action.repository.ActionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActionService {

    private final ActionRepository actionRepository;

    public ActionService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    // CREATE
    public Action createAction(Action action) {
        action.setCreateDate(LocalDate.now());
        action.setLastUpdate(LocalDateTime.now());
        action.setIsDeleted(false);
        if (action.getIsEnabled() == null) {
            action.setIsEnabled(true);
        }
        return actionRepository.save(action);
    }

    // READ ALL (only non-deleted)
    public List<Action> getAllActions() {
        return actionRepository.findAll()
                .stream()
                .filter(a -> a.getIsDeleted() == null || !a.getIsDeleted())
                .toList();
    }

    // READ ONE
    public Action getActionById(Long id) {
        return actionRepository.findById(id).orElse(null);
    }

    // UPDATE
    public Action updateAction(Long id, Action updatedData) {
        Action existing = actionRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setName(updatedData.getName());
        existing.setUserId(updatedData.getUserId());
        existing.setTo(updatedData.getTo());
        existing.setMessage(updatedData.getMessage());
        existing.setActionType(updatedData.getActionType());
        existing.setRunOnTime(updatedData.getRunOnTime());
        existing.setRunOnDay(updatedData.getRunOnDay());
        existing.setCondition(updatedData.getCondition());
        existing.setIsEnabled(updatedData.getIsEnabled());
        existing.setLastUpdate(LocalDateTime.now());   // spec: update timestamp on any change
        return actionRepository.save(existing);
    }

    // DELETE (soft delete)
    public boolean deleteAction(Long id) {
        Action existing = actionRepository.findById(id).orElse(null);
        if (existing == null) {
            return false;
        }
        existing.setIsDeleted(true);                   // spec: don't remove, just flag
        existing.setLastUpdate(LocalDateTime.now());
        actionRepository.save(existing);
        return true;
    }
}
package com.foodlab.detect.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.foodlab.common.result.PageResult;
import com.foodlab.common.result.Result;
import com.foodlab.detect.dto.DetectItemQueryDTO;
import com.foodlab.detect.entity.DetectItem;
import com.foodlab.detect.service.DetectItemService;
import com.foodlab.detect.vo.DetectItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "检测项目管理", description = "检测项目、类别、限量标准管理")
@RestController
@RequestMapping("/api/detect/item")
@RequiredArgsConstructor
public class DetectItemController {

    private final DetectItemService detectItemService;

    @Operation(summary = "分页查询检测项目")
    @GetMapping("/page")
    public Result<PageResult<DetectItem>> getDetectItemPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            DetectItemQueryDTO queryDTO) {
        IPage<DetectItem> page = detectItemService.getDetectItemPage(pageNum, pageSize, queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "获取检测项目详情")
    @GetMapping("/{id}")
    public Result<DetectItemVO> getDetectItemDetail(@PathVariable Long id) {
        return Result.success(detectItemService.getDetectItemDetail(id));
    }

    @Operation(summary = "获取检测项目列表")
    @GetMapping("/list")
    public Result<List<DetectItemVO>> getDetectItemList(
            @RequestParam(required = false) Long categoryId) {
        return Result.success(detectItemService.getDetectItemList(categoryId));
    }

    @Operation(summary = "获取所有检测项目（含限量标准）")
    @GetMapping("/all")
    public Result<List<DetectItemVO>> getAllDetectItems() {
        return Result.success(detectItemService.getAllDetectItems());
    }

    @Operation(summary = "获取动态表单Schema")
    @GetMapping("/form-schema/{detectItemId}")
    public Result<String> getFormSchema(@PathVariable Long detectItemId) {
        return Result.success(detectItemService.getFormSchema(detectItemId));
    }

    @Operation(summary = "新增检测项目")
    @PostMapping
    public Result<Void> addDetectItem(@RequestBody DetectItem detectItem) {
        detectItemService.save(detectItem);
        return Result.success();
    }

    @Operation(summary = "修改检测项目")
    @PutMapping("/{id}")
    public Result<Void> updateDetectItem(@PathVariable Long id, @RequestBody DetectItem detectItem) {
        detectItem.setId(id);
        detectItemService.updateById(detectItem);
        return Result.success();
    }

    @Operation(summary = "删除检测项目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDetectItem(@PathVariable Long id) {
        detectItemService.removeById(id);
        return Result.success();
    }
}

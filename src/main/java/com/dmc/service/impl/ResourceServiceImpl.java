package com.dmc.service.impl;

import com.google.common.base.Strings;
import com.dmc.mapper.ResourceMapper;
import com.dmc.mapper.RoleMapper;
import com.dmc.mapper.UserMapper;
import com.dmc.model.Resource;
import com.dmc.model.SessionInfo;
import com.dmc.model.Tree;
import com.dmc.service.ResourceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("resourceService")
@Transactional
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Tree> tree(SessionInfo sessionInfo) {

        List<Tree> lt = new ArrayList<Tree>();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("resourceTypeId", "0");// 菜单类型的资源

        if (sessionInfo != null) {
            params.put("userId", sessionInfo.getId());// 自查自己有权限的资源
        }

        List<Resource> resourceList = resourceMapper.getResourceList(params);

        for (Resource r : resourceList) {
            Tree tree = new Tree();
            BeanUtils.copyProperties(r, tree);
            tree.setText(r.getName());
            Map<String, Object> attr = new HashMap<String, Object>();
            attr.put("url", r.getUrl());
            tree.setAttributes(attr);
            lt.add(tree);
        }
        return lt;
    }

    @Override
    public List<Tree> allTree(SessionInfo sessionInfo) {
        List<Tree> lt = new ArrayList<Tree>();

        Map<String, Object> params = new HashMap<String, Object>();
        if (sessionInfo != null) {
            params.put("userId", sessionInfo.getId());// 自查自己有权限的资源
        }

        List<Resource> resourceList = resourceMapper.getResourceList(params);

        for (Resource r : resourceList) {
            Tree tree = new Tree();
            BeanUtils.copyProperties(r, tree);
            tree.setText(r.getName());
            Map<String, Object> attr = new HashMap<String, Object>();
            attr.put("url", r.getUrl());
            tree.setAttributes(attr);
            lt.add(tree);
        }
        return lt;
    }

    @Override
    public List<Resource> treeGrid(SessionInfo sessionInfo) {

        Map<String, Object> params = new HashMap<>();
        if (sessionInfo != null) {
            params.put("userId", sessionInfo.getId());// 自查自己有权限的资源
        }

        List<Resource> resourceList = resourceMapper.getResourceList(params);

        Map<String, Resource> map = new HashMap<>();
        resourceList.forEach(resource -> map.put(resource.getId(), resource));
        resourceList.forEach(resource -> resource.setPname(resource.getPid() != null ? map.get(resource.getPid()).getName() : null));

        return resourceList;
    }

    @Override
    public void add(Resource resource, SessionInfo sessionInfo) {

        if (Strings.isNullOrEmpty(resource.getPid())) {
            resource.setPid(null);
        }
        resourceMapper.save(resource);

        // 由于当前用户所属的角色，没有访问新添加的资源权限，所以在新添加资源的时候，将当前资源授权给当前用户的所有角色，以便添加资源后在资源列表中能够找到
        String userId = sessionInfo.getId();
        List<String> roleIds = userMapper.getUserRoleIds(userId);
        roleIds.forEach(roleId -> roleMapper.saveRoleResources(roleId, new String[]{resource.getId()}));

    }

    /**
     * 删除
     *
     * @param id id
     */
    @Override
    public void delete(String id) {
        resourceMapper.deleteById(id);
    }


    @Override
    public void edit(Resource resource) {
        if (Strings.isNullOrEmpty(resource.getPid())) {
            resource.setPid(null);
        }
        resourceMapper.update(resource);
    }


    @Override
    public Resource get(String id) {

        return resourceMapper.getById(id);
    }

}

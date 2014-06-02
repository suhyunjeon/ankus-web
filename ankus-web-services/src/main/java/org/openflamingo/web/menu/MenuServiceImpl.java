package org.openflamingo.web.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public Map<String, List<Menu>> getMenu(Menu menu) {
        Map<String, List<Menu>> menuMap = new HashMap<String, List<Menu>>();
        if (menu.depth == 2)
            menu = menuRepository.selectMenu(menu.parentId);

        menuMap.put("userMenu", menuRepository.selectUserMenu());
        menuMap.put("topMenu", menuRepository.selectTopMenu());
        menuMap.put("subMenu", menuRepository.selectSubMenu(menu.menuId));

        return menuMap;
    }

    @Override
    public Menu getMenu(String topCode, String subCode) {
        return menuRepository.selectMenuByCode(topCode, subCode);
    }
}

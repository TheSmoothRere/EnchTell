package io.github.thesmoothrere.enchtell.config;

import io.github.thesmoothrere.enchtell.Constants;
import io.github.thesmoothrere.enchtell.utils.ShowType;
import io.github.thesmoothrere.relib.api.Config;
import io.github.thesmoothrere.relib.api.ConfigApi;
import io.github.thesmoothrere.relib.config.option.BooleanOption;
import io.github.thesmoothrere.relib.config.option.EnumOption;

@Config(name = Constants.MOD_ID)
public class EnchTellConfig implements ConfigApi {
    private final EnumOption<ShowType> showType = new EnumOption<>("showType", ShowType.ALWAYS);
    private final BooleanOption showOnItems = new BooleanOption("showOnItems", false);

    public EnumOption<ShowType> showType() {
        return showType;
    }

    public BooleanOption showOnItems() {
        return showOnItems;
    }
}

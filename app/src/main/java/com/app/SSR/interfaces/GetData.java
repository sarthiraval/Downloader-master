package com.app.SSR.interfaces;

import java.io.Serializable;
import java.util.ArrayList;

public interface GetData extends Serializable {

    void getData(ArrayList<String> linkList, String message, boolean isData);

}

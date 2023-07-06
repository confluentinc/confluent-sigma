import React from "react";
import {
  Route,
  Routes,
  Navigate
} from "react-router-dom";
import withRouter from "../Wrappers/withRouter"
import classnames from "classnames";
import {Box, IconButton, Link} from '@material-ui/core'
import Icon from '@mdi/react'


// styles
import useStyles from "./styles";

// components
import Header from "../Header";
import Sidebar from "../Sidebar";
import Typography from "../../pages/typography";


// context
import { useLayoutState } from "../../context/LayoutContext";

function Layout() {
  var classes = useStyles();

  // global
  var layoutState = useLayoutState();

  return (
    <div className={classes.root}>
        <>
          <Header />
          <Sidebar />
          <div
            className={classnames(classes.content, {
              [classes.contentShift]: layoutState.isSidebarOpened,
            })}
          >
            <div className={classes.fakeToolbar} />
          </div>
        </>
    </div>
  );
}

export default withRouter(Layout);

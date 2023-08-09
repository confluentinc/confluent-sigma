import React, { useState } from "react";
import {
  Table,
  TableRow,
  TableHead,
  TableBody,
  TableCell
} from "@material-ui/core";
import useStyles from "../styles";
import { withStyles } from "@material-ui/core/styles";

import ExpandIcon from '@mui/icons-material/Expand';
import IconButton from "@mui/material/IconButton";
import SlidingPane from 'react-sliding-pane';
import "./react-sliding-pane.css";
import Editor from "@monaco-editor/react";

export default function TableComponent({ data }) {
  const classes = useStyles();
  const [viewerOpen, setViewerOpen] = useState(false);
  const [selectedData, setSelectedData] = useState({});

  const options = {
    //selectOnLineNumbers: true,
    //roundedSelection: false,
    readOnly: true,
    //cursorStyle: "line",
    //automaticLayout: false,
    minimap: { enabled: false },
  };

  const hanldeExpand = (selectedRec) => {
    setSelectedData(selectedRec);
    setViewerOpen(true)
  };
  
  const StyledTableCell = withStyles({
    root: {
      fontFamily: "Source Code Pro,monospace!important",
      fontSize: "12px",
      lineHeight: "26px!important"
    }
  })(TableCell);

  return (
    <>
      <Table>
        <TableHead>
          <TableRow className={classes.logViewer}>
            <TableCell width="50" key='EXPAND'></TableCell>
            <TableCell width="100" key='TIMESTAMP'>TIMESTAMP</TableCell>
            <TableCell key='TOPIC DATA'>TOPIC DATA</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {data?.map((topicData) => (
            <TableRow className={classes.logViewer}>
              <StyledTableCell>
                <IconButton onClick={() => hanldeExpand(topicData)}>
                <ExpandIcon />
                </IconButton>
              </StyledTableCell>
              <StyledTableCell key='TIMESTAMP'>1234</StyledTableCell>
              <StyledTableCell key='TOPIC DATA'>{JSON.stringify(topicData)}</StyledTableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <SlidingPane
        className="some-custom-class"
        overlayClassName="some-custom-overlay-class"
        isOpen={viewerOpen}
        //title="Hey, it is optional pane title.  I can be React component too."
        subtitle="Optional subtitle."
        width="600px"
        onRequestClose={() => {
          // triggered on "<" on left top click or on outside click
          setViewerOpen(false);
        }}
      >
        <Editor
          //height="50vh"
          defaultLanguage="json"
          defaultValue=""
          value={JSON.stringify(selectedData, null, 3)}
          //onChange={handleEditorChange}
          //theme="vs-dark"
          options={options}
        />
      </SlidingPane>
  </>
  );
}

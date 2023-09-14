import * as React from "react"
import { alpha } from "@mui/material/styles"
import Box from "@mui/material/Box"
import Table from "@mui/material/Table"
import TableBody from "@mui/material/TableBody"
import TableCell from "@mui/material/TableCell"
import TableContainer from "@mui/material/TableContainer"
import TableHead from "@mui/material/TableHead"
import TablePagination from "@mui/material/TablePagination"
import TableRow from "@mui/material/TableRow"
import Toolbar from "@mui/material/Toolbar"
import Typography from "@mui/material/Typography"
import Paper from "@mui/material/Paper"
import UnfoldMoreOutlinedIcon from '@mui/icons-material/ExpandMoreOutlined';
import SlidingPane from 'react-sliding-pane';
import "./react-sliding-pane.css";
import Editor from "@monaco-editor/react";
import { withStyles } from "@material-ui/core/styles";
import { Button } from "@material-ui/core";

function createData(time, data) {
  return {
    time,
    data
  }
}

const headCells = [
  {
    id: "time",
    numeric: false,
    disablePadding: true,
    label: "TIME STAMP"
  },
  {
    id: "topicData",
    numeric: false,
    disablePadding: false,
    label: "TOPIC DATA"
  }
]

function EnhancedTableHead(props) {
  return (
    <TableHead>
      <TableRow>
        <TableCell></TableCell>
        {headCells.map(headCell => (
          <TableCell
            key={headCell.id}
            align={headCell.numeric ? "right" : "left"}
            padding={headCell.disablePadding ? "none" : "normal"}
            sx={{ 
              fontFamily: "Source Code Pro,monospace!important",
              fontSize: 12,
              fontWeight: 'bold' 
            }}
          >
            {headCell.label}
          </TableCell>
        ))}
      </TableRow>
    </TableHead>
  )
}

function EnhancedTableToolbar({title}) {
 
  return (
    <Toolbar >
    <Typography
        sx={{ flex: "1 1 100%" }}
        variant="h6"
        id="tableTitle"
        component="div"
    >
        {title}
    </Typography>
    </Toolbar>
  )
}

export default function DataTable( {title, topicData} ) {
  const [selected, setSelected] = React.useState(-1)
  const [page, setPage] = React.useState(0)
  const [dense, setDense] = React.useState(false)
  const [rowsPerPage, setRowsPerPage] = React.useState(5)
  const [selectedData, setSelectedData] = React.useState({});
  const [viewerOpen, setViewerOpen] = React.useState(false);

  const options = {
    readOnly: true,
    automaticLayout: true,
    minimap: { enabled: false },
  };

  const handleClick = (event, index, row) => {
     setSelected(index)
     setSelectedData(JSON.parse(row.data))
     setViewerOpen(true)
  }

  const handleChangePage = (event, newPage) => {
    setPage(newPage)
  }

  const handleChangeRowsPerPage = event => {
    setRowsPerPage(parseInt(event.target.value, 10))
    setPage(0)
  }

  const isSelected = index => selected == index

  // Avoid a layout jump when reaching the last page with empty rows.
  const emptyRows =
    page > 0 ? Math.max(0, (1 + page) * rowsPerPage - topicData.length) : 0

  const visibleRows = React.useMemo(
    () => 
       topicData.slice(
        page * rowsPerPage,
        page * rowsPerPage + rowsPerPage
      ),
    [topicData.length, page, rowsPerPage]
  )

  return (
    <Box sx={{ width: "100%" }}>
      <Paper sx={{ width: "100%", mb: 2 }}>
        <EnhancedTableToolbar title={title} />
        <TableContainer>
          <Table
            sx={{ minWidth: 50 }}
            aria-labelledby="tableTitle"
            size={dense ? "small" : "medium"}
          >
            <EnhancedTableHead
              rowCount={topicData.length}
            />
            <TableBody>
              {visibleRows.map((row, index) => {
                const isItemSelected = isSelected(index)
                const labelId = `enhanced-table-checkbox-${index}`

                return (
                  <TableRow
                    hover
                    //onClick={event => handleClick(event, index)}
                    role="checkbox"
                    aria-checked={isItemSelected}
                    tabIndex={-1}
                    key={index}
                    selected={isItemSelected}
                    sx={{ cursor: "pointer" }}
                  >
                    <TableCell padding="checkbox">
                      <UnfoldMoreOutlinedIcon
                        fontSize="small"
                        id={labelId}
                        onClick={event => handleClick(event, index, row)}
                        />
                    </TableCell>
                    <TableCell
                      component="th"
                      id={labelId}
                      scope="row"
                      padding="none"
                      width="15%"
                      sx={{ 
                        fontFamily: "Source Code Pro,monospace!important",
                        fontSize: 12,
                      }}
                    >
                      {row.time}
                    </TableCell>
                    <TableCell 
                      align="left"
                      //padding="none"
                      width="15%"
                      sx={{ 
                        fontFamily: "Source Code Pro,monospace!important",
                        fontSize: 12,
                      }}
                    >
                      {row.data}
                    </TableCell>
                  </TableRow>
                )
              })}
              {emptyRows > 0 && (
                <TableRow
                  style={{
                    height: (dense ? 33 : 53) * emptyRows,
                  }}
                >
                  <TableCell colSpan={6} />
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[5, 10, 100]}
          component="div"
          count={topicData.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </Paper>
      <SlidingPane
        isOpen={viewerOpen}
        subtitle="Expanded Topic Data"
        width="600px"
        onRequestClose={() => {
          setViewerOpen(false);
          setSelected(-1);
          setSelectedData();
        }}
      >
        <Editor
          defaultLanguage="json"
          defaultValue=""
          height="100%"
          value={JSON.stringify(selectedData, null, 3)}
          options={options}
        />
      </SlidingPane>
    </Box>
  )
}

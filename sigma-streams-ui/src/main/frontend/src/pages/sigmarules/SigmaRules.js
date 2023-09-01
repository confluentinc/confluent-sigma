import React, { useState, useEffect } from "react";
import { Grid } from "@material-ui/core";
import MUIDataTable from "mui-datatables";
import SigmaRuleSelect from "./SigmaRuleSelect";
import UpdateIcon from "@mui/icons-material/Update";
import Tooltip from "@mui/material/Tooltip";
import IconButton from "@mui/material/IconButton";
import AddIcon from '@mui/icons-material/Add';
import { Link } from "react-router-dom";

// components
import PageTitle from "../../components/PageTitle/PageTitle";

var columns = [
  {label: "Title", name: "title"},
  {label: "Description", name: "description"},
  {label: "Author", name: "author"},
  {label: "Product", name: "logsource.product"},
  {label: "Service", name: "logsource.service"}
];

export default function SigmaRules(props) {
  //const { classes } = props;
  const [tableData, setTableData] = useState([])
  const SERVER_ENDPOINT = process.env.REACT_APP_SERVER_ENDPOINT;

  const refreshTable = async () => {
    try {
        const data = await (await fetch(SERVER_ENDPOINT + "sigmaRules")).json()
        setTableData(data);
        console.log(tableData);
    } catch (err) {
        console.log(err.message)
    }
  }

  const addRule = async () => {
    try {
        const data = await (await fetch(SERVER_ENDPOINT + "sigmaRules")).json()
        setTableData(data);
        console.log(tableData);
    } catch (err) {
        console.log(err.message)
    }
  }

  useEffect(() => { refreshTable() }, []);

  const handleRowClick = (rowData, rowState) => {
    console.log(rowData, rowState);
  }

  const options= {
    filterType: "checkbox",
    selectableRows: 'single',
    enableNestedDataAccess: '.',
    onRowClick: handleRowClick,
    customToolbarSelect: (selectedRows, displayData, setSelectedRows) => (
      <SigmaRuleSelect selectedRows={selectedRows} displayData={displayData} setSelectedRows={setSelectedRows} />
    ),
    customToolbar: () => {
      return (
        <>
        <Tooltip title={"Create New Rule"}>
          <Link to="/app/sigmaruleeditor">
              <IconButton className={props.iconButton} onClick={addRule}>
                <AddIcon className={props.deleteIcon} />
              </IconButton>
          </Link>
        </Tooltip>
        <Tooltip title={"Refresh Table"}>
        <IconButton className={props.iconButton} onClick={refreshTable}>
          <UpdateIcon className={props.deleteIcon} />
        </IconButton>
      </Tooltip>
      </>
    );
    }
  }

  const components = {
    icons: {
      UpdateIcon
    }
  };

  //refreshTable();
  return (
    <>
      <PageTitle title="Sigma Rules" />
      <Grid container spacing={4}>
        <Grid item xs={12}>
          <MUIDataTable
            title=""
            data={tableData}
            columns={columns}
            options={options}
            components={components}
          />
        </Grid>
      </Grid>
    </>
  );
}
